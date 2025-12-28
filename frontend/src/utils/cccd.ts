
export interface CCCDData {
  cccd: string;
  ten: string;
  ngaySinh: number | null;
  gioiTinh: 'MALE' | 'FEMALE';
  diaChi: string;
  provinceName: string;
}

export function normalizeString(str: string): string {
  let normalized = str
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d");
    
  // Remove common prefixes
  normalized = normalized.replace(/^(tỉnh|thành phố|tp\.?|t\.?|th\.?)\s+/i, "");
  
  // Remove special chars (keep alphanumeric and spaces)
  normalized = normalized.replace(/[^a-z0-9\s]/g, " ");
  
  // Collapse spaces
  return normalized.replace(/\s+/g, " ").trim();
}

export function getAcronym(str: string): string {
  return str.split(/\s+/).map(w => w[0]).join('');
}

export const provinceMappings: Record<string, string> = {
  'tphcm': 'ho chi minh',
  'tp hcm': 'ho chi minh',
  'hn': 'ha noi',
  'dn': 'da nang',
  'brvt': 'ba ria vung tau',
  'vung tau': 'ba ria vung tau',
  'ba ria': 'ba ria vung tau',
  'dak lak': 'dac lac',
  'dak nong': 'dac nong',
  'thua thien hue': 'thua thien hue',
  'tt hue': 'thua thien hue'
};

export function parseCCCDQrContent(content: string): CCCDData | null {
  try {
    // Format: ID|Old_ID|Name|DOB|Gender|Address|Issue_Date
    const parts = content.split('|');
    if (parts.length < 6) return null;

    const cccd = parts[0];
    const ten = parts[2];
    
    // DOB: ddMMyyyy
    let ngaySinh: number | null = null;
    const dobStr = parts[3];
    if (dobStr && dobStr.length === 8) {
      const day = parseInt(dobStr.substring(0, 2));
      const month = parseInt(dobStr.substring(2, 4)) - 1;
      const year = parseInt(dobStr.substring(4, 8));
      ngaySinh = new Date(year, month, day).getTime();
    }
    
    const gioiTinh = parts[4] === 'Nam' ? 'MALE' : 'FEMALE';
    const fullAddress = parts[5];
    
    const addressParts = fullAddress.split(',').map(p => p.trim()).filter(p => p.length > 0);
    let provinceName = '';
    
    if (addressParts.length >= 1) {
      provinceName = addressParts[addressParts.length - 1];
    }
    
    return {
      cccd,
      ten,
      ngaySinh,
      gioiTinh,
      diaChi: fullAddress,
      provinceName
    };
  } catch (e) {
    console.error('Error parsing CCCD content:', e);
    return null;
  }
}
