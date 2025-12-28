export function normalizeTinh(value: string): string {
  if (!value) return '';

  let s = value.trim().toLowerCase();

  // Bỏ tiền tố
  s = s.replace(/^tỉnh |^tp |^thành phố |^thanh pho |^tthp /, '');

  // Map viết tắt phổ biến
  const map: Record<string, string> = {
    'hcm': 'thành phố hồ chí minh',
    'tphcm': 'thành phố hồ chí minh',
    'tp.hcm': 'thành phố hồ chí minh',
    'sai gon': 'thành phố hồ chí minh',
    'sg': 'thành phố hồ chí minh',

    'hn': 'hà nội',
    'tp.hn': 'hà nội',

    'dn': 'đà nẵng',
    'danang': 'đà nẵng',

    'brvt': 'bà rịa - vũng tàu',
    'ba ria vung tau': 'bà rịa - vũng tàu',
    'baria vungtau': 'bà rịa - vũng tàu',
    'vung tau': 'bà rịa - vũng tàu',

    'lao cai': 'lào cai',
    'quang ninh': 'quảng ninh',
    'dak lak': 'đắk lắk',
    'dak nong': 'đắk nông',
    'kontum': 'kon tum',
  };

  if (map[s]) return map[s];

  // Chuẩn hóa các ký tự thiếu dấu
  s = s
    .replace(/a/g, 'a')
    .replace(/e/g, 'e')
    .replace(/i/g, 'i')
    .replace(/o/g, 'o')
    .replace(/u/g, 'u')
    .replace(/y/g, 'y');

  return s.trim();
}
 export function normalize(name: string) {
  return name.toLowerCase().replace(/^phường |^xã |^thị trấn|^thôn /, '').trim();
}