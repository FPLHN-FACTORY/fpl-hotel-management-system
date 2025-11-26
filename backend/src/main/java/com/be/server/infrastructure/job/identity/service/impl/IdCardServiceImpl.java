package com.be.server.infrastructure.job.identity.service.impl;

import com.be.server.infrastructure.job.identity.model.IdCardData;
import com.be.server.infrastructure.job.identity.service.IdCardService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IdCardServiceImpl implements IdCardService {
    private static final Logger logger = LoggerFactory.getLogger(IdCardServiceImpl.class);
    // Định dạng ngày cho dữ liệu OCR (thường là dd/MM/yyyy)
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    // Định dạng ngày cho dữ liệu QR (thường là DDMMYYYY)
    private static final SimpleDateFormat QR_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");

    @Value("${TESSDATA_PREFIX:}")
    private String tessDataPath;

    @Override
    public IdCardData scanIdCard(MultipartFile file, String qrData) throws Exception {
        logger.info("Starting ID card scan for file: {} with QR data presence: {}",
                file.getOriginalFilename(), qrData != null);

        validateFile(file);

        IdCardData result = new IdCardData();

        // 1. Ưu tiên xử lý Dữ liệu QR nếu có
        if (qrData != null && !qrData.trim().isEmpty()) {
            logger.info("Attempting to parse data from QR code.");
            try {
                result = parseQrData(qrData);
                logger.info("Successfully parsed data from QR code.");
                // Nếu QR đã trích xuất được số CCCD và Tên, ta tin tưởng và kết thúc
                if (result.getIdNumber() != null && result.getFullName() != null) {
                    return result;
                }
            } catch (Exception e) {
                logger.warn("QR data parsing failed, falling back to OCR: {}", e.getMessage());
            }
        }

        // 2. Thực hiện OCR để bổ sung hoặc lấy toàn bộ thông tin
        try {
            BufferedImage image = convertToBufferedImage(file);
            String ocrText = performOCR(image);

            IdCardData ocrResult = parseIdCardText(ocrText);

            // Bổ sung các trường thiếu từ OCR vào result (từ QR)
            if (result.getIdNumber() == null) result.setIdNumber(ocrResult.getIdNumber());
            if (result.getFullName() == null) result.setFullName(ocrResult.getFullName());
            if (result.getDateOfBirth() == null) result.setDateOfBirth(ocrResult.getDateOfBirth());
            if (result.getGender() == null) result.setGender(ocrResult.getGender());
            if (result.getAddress() == null) result.setAddress(ocrResult.getAddress());
            if (result.getProvince() == null) result.setProvince(ocrResult.getProvince());

            logger.debug("Final parsed ID card data: {}", result);
            return result;

        } catch (TesseractException e) {
            logger.error("OCR processing failed", e);
            throw new Exception("Failed to process image with OCR: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Image processing error", e);
            throw new Exception("Error processing image: " + e.getMessage());
        }
    }

    /**
     * Phân tích dữ liệu từ chuỗi QR code (theo cấu trúc CCCD)
     */
    private IdCardData parseQrData(String qrData) throws ParseException {
        IdCardData data = new IdCardData();
        // Cấu trúc CCCD: Số CCCD|Họ và tên|Ngày sinh DDMMYYYY|Giới tính|Địa chỉ thường trú|...
        String[] parts = qrData.split("\\|");

        if (parts.length < 5) {
            throw new IllegalArgumentException("QR data has insufficient fields. Expected at least 5.");
        }

        data.setIdNumber(parts[0].trim());
        data.setFullName(parts[1].trim());

        // parts[2] là ngày sinh (DDMMYYYY)
        try {
            // Sử dụng đối tượng QR_DATE_FORMAT tĩnh
            data.setDateOfBirth(QR_DATE_FORMAT.parse(parts[2].trim()));
        } catch (ParseException e) {
            logger.warn("Failed to parse date from QR: {}", parts[2]);
        }

        data.setGender(parts[3].trim());

        // parts[4] là Địa chỉ thường trú đầy đủ
        data.setAddress(parts[4].trim());

        return data;
    }

    private void validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are supported");
        }
    }

    private BufferedImage convertToBufferedImage(MultipartFile file) throws IOException {
        try (InputStream in = new ByteArrayInputStream(file.getBytes())) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new IOException("Unsupported image format");
            }

            // Step 1: Convert to grayscale
            BufferedImage processedImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY);

            // Convert to grayscale using color conversion
            ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), 
                null
            );
            op.filter(image, processedImage);

            // Step 2: Enhance contrast
            float contrast = 1.5f;
            float brightness = 0.0f;
            RescaleOp rescaleOp = new RescaleOp(contrast, brightness, null);
            processedImage = rescaleOp.filter(processedImage, null);

            // Step 3: Apply adaptive thresholding with dithering
            BufferedImage binaryImage = new BufferedImage(
                    processedImage.getWidth(),
                    processedImage.getHeight(),
                    BufferedImage.TYPE_BYTE_BINARY);

            // Create a custom dithering operation
            BufferedImageOp ditherOp = new BufferedImageOp() {
                @Override
                public BufferedImage filter(BufferedImage src, BufferedImage dest) {
                    if (dest == null) {
                        dest = createCompatibleDestImage(src, null);
                    }

                    int width = src.getWidth();
                    int height = src.getHeight();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int rgb = src.getRGB(x, y);
                            int gray = (rgb >> 16) & 0xFF; // Assuming grayscale, so R=G=B
                            int newPixel = gray < 128 ? 0x000000 : 0xFFFFFF;
                            dest.setRGB(x, y, newPixel);
                        }
                    }
                    return dest;
                }

                @Override
                public Rectangle2D getBounds2D(BufferedImage src) {
                    return new Rectangle(0, 0, src.getWidth(), src.getHeight());
                }

                @Override
                public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
                    if (destCM == null) {
                        destCM = src.getColorModel();
                    }
                    return new BufferedImage(
                        destCM,
                        destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
                        destCM.isAlphaPremultiplied(),
                        null
                    );
                }

                @Override
                public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
                    if (dstPt == null) {
                        dstPt = new Point2D.Float();
                    }
                    dstPt.setLocation(srcPt.getX(), srcPt.getY());
                    return dstPt;
                }

                @Override
                public RenderingHints getRenderingHints() {
                    return null;
                }
            };

            return ditherOp.filter(processedImage, binaryImage);
        }
    }

    private String findTessDataPath() {
        // First, try to use the configured path
        if (tessDataPath != null && !tessDataPath.trim().isEmpty()) {
            File tessDataDir = new File(tessDataPath, "tessdata");
            if (tessDataDir.exists() && tessDataDir.isDirectory()) {
                logger.info("Using configured tessdata directory: {}", tessDataDir.getAbsolutePath());
                return tessDataDir.getAbsolutePath();
            }
        }

        // Try to find it in the resources directory
        File resourcesDir = new File("src/main/resources/tessdata");
        if (resourcesDir.exists() && resourcesDir.isDirectory()) {
            logger.info("Found tessdata in resources directory: {}", resourcesDir.getAbsolutePath());
            return resourcesDir.getAbsolutePath();
        }

        // Try to find it in the current working directory
        File currentDir = new File("tessdata");
        if (currentDir.exists() && currentDir.isDirectory()) {
            logger.info("Found tessdata in current directory: {}", currentDir.getAbsolutePath());
            return currentDir.getAbsolutePath();
        }

        // If not found, try to extract from JAR
        try {
            return extractTessDataFromJar();
        } catch (Exception e) {
            logger.error("Error extracting tessdata from JAR", e);
        }

        // Fallback to current directory
        return ".";
    }

    private String extractTessDataFromJar() throws Exception {
        // Define required tessdata files
        String[] requiredFiles = {"vie.traineddata", "osd.traineddata"};

        // Create temp directory if it doesn't exist
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tessdata_temp");
        logger.info("Using temporary tessdata directory: {}", tempDir.getAbsolutePath());

        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new RuntimeException("Could not create temporary tessdata directory: " + tempDir.getAbsolutePath());
        }

        // Check if all required files already exist
        boolean allFilesExist = true;

        // Check if all required files already exist
        for (String fileName : requiredFiles) {
            if (!new File(tempDir, fileName).exists()) {
                allFilesExist = false;
                break;
            }
        }

        if (allFilesExist) {
            logger.info("Using existing tessdata files in temp directory");
            return tempDir.getAbsolutePath();
        }

        // Extract files from resources if not all exist
        logger.info("Extracting tessdata to temporary directory");
        for (String fileName : requiredFiles) {
            try (InputStream is = getClass().getResourceAsStream("/tessdata/" + fileName)) {
                if (is == null) {
                    logger.error("Required tessdata file not found in classpath: /tessdata/{}", fileName);
                    continue;
                }

                File outputFile = new File(tempDir, fileName);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    logger.debug("Successfully extracted {}", fileName);
                }
            } catch (IOException e) {
                logger.error("Failed to extract tessdata file: {}", fileName, e);
            }
        }

        return tempDir.getAbsolutePath();
    }

// Trong IdCardServiceImpl.java

    private String performOCR(BufferedImage image) throws TesseractException {
        ITesseract tesseract = new Tesseract();
        String result = ""; // Khởi tạo biến kết quả

        try {
            // Lấy đường dẫn tessdata (Bạn cần đảm bảo hàm findTessDataPath() đã được sửa để hoạt động đúng)
            String tessDataPath = findTessDataPath();
            logger.info("Using tessdata path: {}", tessDataPath);

            // --- 1. KIỂM TRA TỆP VÀ CẤU HÌNH ---

            File tessDataDir = new File(tessDataPath);
            if (!tessDataDir.exists() || !tessDataDir.isDirectory()) {
                throw new TesseractException("Tessdata directory not found: " + tessDataPath);
            }

            // Cấu hình Tesseract
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("vie");
            tesseract.setOcrEngineMode(1); // LSTM only - Độ chính xác cao
            tesseract.setTessVariable("user_defined_dpi", "300"); // Đặt DPI

            // Vô hiệu hóa từ điển (quan trọng cho Số CCCD và Ngày sinh)
            tesseract.setTessVariable("load_system_dawg", "0");
            tesseract.setTessVariable("load_freq_dawg", "0");
            tesseract.setTessVariable("load_punc_dawg", "0");
            tesseract.setTessVariable("load_number_dawg", "0");
            tesseract.setTessVariable("load_unambig_dawg", "0");

            // Danh sách trắng ký tự (Bao gồm Tiếng Việt có dấu)
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯưẠ-ỹ. ,/()\"'-:");

            logger.info("Starting OCR processing with optimized configuration.");

            // --- 2. THỰC HIỆN OCR VỚI LOGIC THỬ LẠI ---

            int maxRetries = 3;

            for (int i = 0; i < maxRetries; i++) {
                // Đặt PSM cho lần thử hiện tại
                int currentPsm;
                if (i == 0) {
                    currentPsm = 6; // PSM_SINGLE_BLOCK (Tốt cho bố cục cố định)
                } else if (i == 1) {
                    currentPsm = 4; // PSM_SINGLE_COLUMN (Thử nghiệm 1)
                } else {
                    currentPsm = 3; // PSM_AUTO (Thử nghiệm 2 - Tự động phát hiện bố cục)
                }
                tesseract.setPageSegMode(currentPsm);

                try {
                    logger.warn("OCR Attempt {}/{}: Using PSM {}", i + 1, maxRetries, currentPsm);
                    result = tesseract.doOCR(image);

                    // Kiểm tra kết quả: nếu chuỗi dài hơn 10 ký tự, coi là thành công
                    if (result != null && result.trim().length() > 10) {
                        logger.info("OCR completed successfully after attempt {}", i + 1);
                        return result.trim(); // <<< RETURN THÀNH CÔNG >>>
                    }

                    logger.warn("OCR returned insufficient text (length < 10), trying new PSM...");

                } catch (Exception e) {
                    logger.warn("OCR attempt {} failed: {}", i + 1, e.getMessage());
                    if (i == maxRetries - 1) {
                        throw e; // Ném ngoại lệ ra ngoài trên lần thử cuối cùng
                    }
                }
            }

            logger.error("OCR failed to extract sufficient text after {} attempts.", maxRetries);
            throw new TesseractException("OCR failed to extract sufficient text from the image."); // <<< THROW KHI THẤT BẠI HOÀN TOÀN >>>

        } catch (Exception e) {
            logger.error("OCR processing failed", e);
            throw new TesseractException("OCR processing failed: " + e.getMessage(), e);
        }
    }
    private IdCardData parseIdCardText(String text) {
        IdCardData data = new IdCardData();
        if (text == null || text.trim().isEmpty()) {
            return data;
        }

        try {
            // Số CCCD: 12 chữ số
            data.setIdNumber(extractPattern(text, "\\b(\\d{12})\\b", 1));

            // Ngày sinh: tìm định dạng dd/MM/yyyy
            String dobPattern = "\\b(Ng\u00e0y sinh|Date of Birth)\\s*:?([0-9\\/\\-.]+)\\b";
            String dobMatch = extractPattern(text, dobPattern, 2);
            data.setDateOfBirth(parseDate(dobMatch));

            // Giới tính
            data.setGender(extractGender(text));

            // Họ và tên: thường là chuỗi chữ hoa sau nhãn "Họ và tên"
            String namePattern = "(Họ\\s*và\\s*tên|Họ\\s*va\\s*ten|Full\\s*name)[:\\s]*([\\p{L}\\s\\d\\/\\-.]+)(?=\\n|\\r|\\Z)";
            data.setFullName(extractPattern(text, namePattern, 2));
            if (data.getFullName() == null || data.getFullName().trim().isEmpty()) {
                String[] lines = text.split("\\r?\\n");
                if (lines.length > 0) {
                    String possibleName = extractValue(lines[0]);
                    // Kiểm tra nếu dòng đầu tiên KHÔNG phải là tiêu đề (ví dụ: CỘNG HOÀ XÃ HỘI CHỦ NGHĨA VIỆT NAM)
                    if (possibleName != null && possibleName.length() > 5 && !possibleName.contains("CỘNG HOÀ")) {
                        data.setFullName(possibleName);
                    }
                }
            }
            // Địa chỉ thường trú
            String addressPattern = "(Địa chỉ thường trú|Permanent address)\\s*:?(.*?)(?=\\n|\\r|\\Z)";
            data.setAddress(extractPattern(text, addressPattern, 2));

            // Tỉnh/Thành phố
            if (data.getAddress() != null) {
                // Thử trích xuất từ địa chỉ, lấy phần cuối cùng
                String provinceMatch = extractPattern(data.getAddress(), "(T\u1ec9nh|Th\u00e0nh ph\u1ed1)\\s+([^,\\n]+)$", 2);
                data.setProvince(provinceMatch);
            }

        } catch (Exception e) {
            logger.warn("Error parsing ID card text", e);
        }

        return data;
    }

    private String extractValue(String text) {
        return text != null ? text.trim() : null;
    }

    /**
     * Hàm dùng Regex để trích xuất giá trị dựa trên nhóm bắt (Capturing Group)
     *
     * @param text       Chuỗi đầu vào (Văn bản OCR)
     * @param pattern    Biểu thức chính quy
     * @param groupIndex Index của nhóm muốn trích xuất (1, 2,...)
     * @return Giá trị được trích xuất
     */
    private String extractPattern(String text, String pattern, int groupIndex) {
        if (text == null) return null;
        // Sử dụng cờ DOTALL để . khớp với cả ký tự xuống dòng
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(groupIndex).trim() : null;
    }

    // Phiên bản quá tải cho các pattern đơn giản
    private String extractPattern(String text, String pattern) {
        return extractPattern(text, pattern, 0); // Group 0 là toàn bộ chuỗi khớp
    }


    /**
     * Phân tích chuỗi ngày tháng (dd/MM/yyyy)
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            // Chuẩn hóa dấu phân cách thành / trước khi parse
            String normalizedDate = dateStr.replaceAll("[\\/\\-.]", "/");
            // Sử dụng đối tượng DATE_FORMAT tĩnh
            return DATE_FORMAT.parse(normalizedDate);
        } catch (ParseException e) {
            logger.debug("Failed to parse date: " + dateStr, e);
            return null;
        }
    }

    private String extractGender(String text) {
        if (text.contains("Nam")) return "Nam";
        if (text.contains("Nữ")) return "Nữ";
        return null;
    }
}