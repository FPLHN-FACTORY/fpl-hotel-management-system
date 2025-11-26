import axios from 'axios';

interface Address {
  name: string;
  code: string;
  parentCode?: string;
  type: 'tinh' | 'thanh-pho' | 'huyen' | 'xa';
}

// Updated list of 28 provinces and 6 centrally run cities after administrative reorganization
const PROVINCES: Address[] = [
  // 6 centrally run cities
  { name: 'Hà Nội', code: '01', type: 'thanh-pho' },  // No boundary changes
  { name: 'Hồ Chí Minh', code: '79', type: 'thanh-pho' },  // Merged with Bình Dương, Bà Rịa - Vũng Tàu
  { name: 'Hải Phòng', code: '31', type: 'thanh-pho' },  // Merged with Hải Dương
  { name: 'Đà Nẵng', code: '48', type: 'thanh-pho' },  // Merged with Quảng Nam
  { name: 'Huế', code: '46', type: 'thanh-pho' },  // New centrally run city (Thừa Thiên Huế)
  { name: 'Cần Thơ', code: '92', type: 'thanh-pho' },  // Merged with Sóc Trăng, Hậu Giang
  
  // 28 provinces
  // Unchanged provinces
  { name: 'Lai Châu', code: '12', type: 'tinh' },
  { name: 'Điện Biên', code: '11', type: 'tinh' },
  { name: 'Sơn La', code: '14', type: 'tinh' },
  { name: 'Lạng Sơn', code: '20', type: 'tinh' },
  { name: 'Quảng Ninh', code: '22', type: 'tinh' },
  { name: 'Thanh Hóa', code: '38', type: 'tinh' },
  { name: 'Nghệ An', code: '40', type: 'tinh' },
  { name: 'Hà Tĩnh', code: '42', type: 'tinh' },
  { name: 'Cao Bằng', code: '04', type: 'tinh' },
  
  // Merged provinces
  { name: 'Tuyên Quang', code: '08', type: 'tinh' },  // Hà Giang + Tuyên Quang
  { name: 'Lào Cai', code: '10', type: 'tinh' },  // Lào Cai + Yên Bái
  { name: 'Thái Nguyên', code: '19', type: 'tinh' },  // Bắc Kạn + Thái Nguyên
  { name: 'Phú Thọ', code: '25', type: 'tinh' },  // Vĩnh Phúc + Hòa Bình + Phú Thọ
  { name: 'Bắc Ninh', code: '27', type: 'tinh' },  // Bắc Giang + Bắc Ninh
  { name: 'Hưng Yên', code: '33', type: 'tinh' },  // Thái Bình + Hưng Yên
  { name: 'Ninh Bình', code: '37', type: 'tinh' },  // Nam Định + Ninh Bình
  { name: 'Quảng Trị', code: '45', type: 'tinh' },  // Quảng Bình + Quảng Trị
  { name: 'Quảng Ngãi', code: '51', type: 'tinh' },  // Kon Tum + Quảng Ngãi
  { name: 'Gia Lai', code: '64', type: 'tinh' },  // Gia Lai + Bình Định
  { name: 'Khánh Hòa', code: '56', type: 'tinh' },  // Ninh Thuận + Khánh Hòa
  { name: 'Lâm Đồng', code: '68', type: 'tinh' },  // Đắk Nông + Bình Thuận + Lâm Đồng
  { name: 'Đắk Lắk', code: '66', type: 'tinh' },  // Phú Yên + Đắk Lắk
  { name: 'Đồng Nai', code: '75', type: 'tinh' },  // Bình Phước + Đồng Nai
  { name: 'Tây Ninh', code: '72', type: 'tinh' },  // Long An + Tây Ninh
  { name: 'Vĩnh Long', code: '86', type: 'tinh' },  // Bến Tre + Trà Vinh + Vĩnh Long
  { name: 'Đồng Tháp', code: '87', type: 'tinh' },  // Tiền Giang + Đồng Tháp
  { name: 'Cà Mau', code: '96', type: 'tinh' },  // Bạc Liêu + Cà Mau
  { name: 'An Giang', code: '89', type: 'tinh' }  // Kiên Giang + An Giang
];

let communes: Record<string, Address[]> = {};

// Get all provinces
export async function fetchProvinces(): Promise<Address[]> {
  return [...PROVINCES];
}

// Fetch communes for a province
export async function fetchCommunes(provinceCode: string): Promise<Address[]> {
  if (communes[provinceCode]) return communes[provinceCode];
  
  try {
    const allCommunes: Address[] = [];
    
    // Get all districts for the province
    const districtResponse = await axios.get(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
    
    // For each district, get its wards
    for (const district of districtResponse.data.districts || []) {
      try {
        const wardResponse = await axios.get(`https://provinces.open-api.vn/api/d/${district.code}?depth=2`);
        
        if (wardResponse.data.wards?.length > 0) {
          wardResponse.data.wards.forEach((ward: any) => {
            allCommunes.push({
              name: ward.name,
              code: ward.code.toString(),
              parentCode: district.code.toString(),
              type: 'xa'
            });
          });
        }
      } catch (error) {
        console.error(`Error fetching wards for district ${district.code}:`, error);
        // Continue with next district even if one fails
        continue;
      }
    }
    
    // Cache the results
    communes[provinceCode] = allCommunes;
    return allCommunes;
  } catch (error) {
    console.error('Error in fetchCommunes:', error);
    return [];
  }
}

// Get address name by code
export async function getAddressName(code: string, type: 'tinh' | 'huyen' | 'xa', parentCode?: string): Promise<string> {
  if (!code) return '';
  
  try {
    if (type === 'tinh') {
      const provinces = await fetchProvinces();
      const province = provinces.find(p => p.code === code);
      return province?.name || code;
    } else if (type === 'xa' && parentCode) {
      const communes = await fetchCommunes(parentCode);
      const commune = communes.find(c => c.code === code);
      return commune?.name || code;
    }
    return ''; // Return empty string for district (huyen) as it's no longer used
  } catch (error) {
    console.error(`Error getting ${type} name:`, error);
    return code;
  }
}

// Format full address
export async function formatFullAddress(
  diaChi: string,
  xaCode: string,
  _huyenCode: string, // Keep for backward compatibility but don't use
  tinhCode: string
): Promise<string> {
  const [xa, tinh] = await Promise.all([
    getAddressName(xaCode, 'xa', tinhCode), // Directly use tinhCode as parent
    getAddressName(tinhCode, 'tinh')
  ]);
  
  return [diaChi, xa, tinh].filter(Boolean).join(', ') || '--';
}
