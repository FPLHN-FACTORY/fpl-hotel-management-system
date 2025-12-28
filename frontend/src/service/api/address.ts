import axios from 'axios';
import { normalizeString, provinceMappings } from '@/utils/cccd';

export interface Province {
  province_code: string;
  name: string;
  full_name?: string; 
  code_name?: string;
}

export interface Ward {
  ward_code: string;
  ward_name: string;
  district_code?: string;
  province_code: string;
  full_name?: string; 
  code_name?: string;
}

export interface ConvertedAddress {
    tinh_code: string;
    xa_code: string;
    dia_chi_duong: string;
    tinh_name: string;
    xa_name: string;
}

const OPEN_API_URL = 'https://provinces.open-api.vn/api';
const TINH_THANH_PHO_API_URL = 'https://tinhthanhpho.com/api/v1';
const API_KEY = 'hvn_1cDiF6lyeG9thFR9CjjOWyGWz8RIusCC'; // TODO: Nhập API Key của bạn vào đây

// Cache to avoid repeated requests
let provincesCache: any[] | null = null;

/**
 * Lấy danh sách tất cả Tỉnh/Thành phố (dùng Open API cho dropdown)
 */
export async function getProvinces(): Promise<Province[]> {
  try {
    if (provincesCache) {
      return provincesCache.map((p: any) => ({
        province_code: String(p.code),
        name: p.name,
        full_name: p.name,
        code_name: p.codename
      }));
    }

    const response = await axios.get(`${OPEN_API_URL}/p/`);
    const data = response.data;

    if (Array.isArray(data)) {
      provincesCache = data;
      return data.map((province: any) => ({
        province_code: String(province.code),
        name: province.name,
        full_name: province.name,
        code_name: province.codename
      }));
    }
    return [];
  } catch (error) {
    console.error('Error fetching provinces:', error);
    throw error;
  }
}

/**
 * Lấy danh sách Phường/Xã
 */
export async function getWards(provinceCode: string): Promise<Ward[]> {
  try {
    const response = await axios.get(`${OPEN_API_URL}/p/${provinceCode}?depth=3`);
    const provinceData = response.data;

    if (provinceData && Array.isArray(provinceData.districts)) {
      const allWards: Ward[] = [];
      
      provinceData.districts.forEach((district: any) => {
        if (Array.isArray(district.wards)) {
          district.wards.forEach((ward: any) => {
            allWards.push({
              ward_code: String(ward.code),
              ward_name: ward.name,
              district_code: String(district.code),
              province_code: String(provinceData.code),
              full_name: ward.name,
              code_name: ward.codename
            });
          });
        }
      });

      return allWards;
    }
    return [];
  } catch (error) {
    console.error('Error fetching wards:', error);
    throw error;
  }
}

/**
 * Helper: Strip administrative prefixes for better matching
 */
function stripPrefix(name: string): string {
    return normalizeString(name)
        .replace(/^(tinh|thanh pho|tp|quan|huyen|thi xa|phuong|xa|thi tran)\s+/g, '')
        .trim();
}

/**
 * Helper: Format code to standard GSO format (padded string)
 * Province: 2 digits, District: 3 digits, Ward: 5 digits
 */
function formatCode(code: string | number, length: number): string {
    return String(code).padStart(length, '0');
}

/**
 * CHỨC NĂNG CHÍNH: 
 * 1. Parse địa chỉ thô -> Mã (Old Codes) dùng Open API data.
 * 2. Gọi tinhthanhpho.com API để chuyển đổi sang cấu trúc mới (nếu có API Key).
 */
export async function convertFullAddress(fullAddress: string): Promise<ConvertedAddress | null> {
    try {
        if (!fullAddress) return null;

        // 1. Ensure we have provinces loaded for local parsing
        if (!provincesCache) {
            await getProvinces();
        }
        if (!provincesCache) return null;

        // 2. Parse locally to find Old Codes
        const parts = fullAddress.split(',').map(p => p.trim()).filter(p => p);
        if (parts.length === 0) return null;

        const provincePart = parts[parts.length - 1];
        const normalizedProvincePart = normalizeString(provincePart);

        // Find Province
        let foundProvince = provincesCache.find((p: any) => {
            const pNameNorm = normalizeString(p.name);
            const pNameStripped = stripPrefix(p.name);
            return pNameNorm.includes(normalizedProvincePart) || 
                   normalizedProvincePart.includes(pNameNorm) ||
                   pNameStripped === normalizedProvincePart ||
                   normalizedProvincePart.includes(pNameStripped);
        });

        // Fallback mappings
        if (!foundProvince) {
            for (const [key, value] of Object.entries(provinceMappings)) {
                if (normalizedProvincePart.includes(key) || key.includes(normalizedProvincePart)) {
                    foundProvince = provincesCache.find((p: any) => normalizeString(p.name).includes(value));
                    if (foundProvince) break;
                }
            }
        }

        if (!foundProvince) {
            console.warn('Could not find province locally for:', provincePart);
            return null;
        }

        // Fetch details to find District/Ward
        const response = await axios.get(`${OPEN_API_URL}/p/${foundProvince.code}?depth=3`);
        const provinceDetails = response.data;

        let foundWard: any = null;
        let foundDistrict: any = null;

        const addressWithoutProvince = parts.slice(0, -1).join(', ');
        const normalizedAddress = normalizeString(addressWithoutProvince);

        if (provinceDetails.districts) {
            const districts = [...provinceDetails.districts].sort((a: any, b: any) => b.name.length - a.name.length);

            for (const district of districts) {
                const dNameStripped = stripPrefix(district.name);
                if (normalizedAddress.includes(dNameStripped)) {
                    foundDistrict = district;
                    if (district.wards) {
                         const wards = [...district.wards].sort((a: any, b: any) => b.name.length - a.name.length);
                         for (const ward of wards) {
                             const wNameStripped = stripPrefix(ward.name);
                             if (normalizedAddress.includes(wNameStripped)) {
                                 foundWard = ward;
                                 break;
                             }
                         }
                    }
                    if (foundWard) break;
                }
            }
            
            // Fallback: search ward directly
            if (!foundDistrict && !foundWard) {
                 for (const district of districts) {
                    if (district.wards) {
                        for (const ward of district.wards) {
                            const wNameStripped = stripPrefix(ward.name);
                            if (normalizedAddress.includes(wNameStripped)) {
                                foundWard = ward;
                                foundDistrict = district;
                                break;
                            }
                        }
                    }
                    if (foundWard) break;
                 }
            }
        }

        // Base result (Old/Current Address)
        const result: ConvertedAddress = {
            tinh_code: String(foundProvince.code),
            tinh_name: foundProvince.name,
            xa_code: foundWard ? String(foundWard.code) : '',
            xa_name: foundWard ? foundWard.name : '',
            dia_chi_duong: parts[0]
        };

        // Refine street address
        if (parts.length > 1) {
             result.dia_chi_duong = parts.slice(0, parts.length - 1).join(', ');
             if (foundDistrict) {
                 const dNameStripped = stripPrefix(foundDistrict.name);
                 const subParts = result.dia_chi_duong.split(',').map(s => s.trim());
                 const lastSub = subParts[subParts.length - 1];
                 if (normalizeString(lastSub).includes(dNameStripped)) {
                     result.dia_chi_duong = subParts.slice(0, -1).join(', ');
                 }
             }
             if (foundWard) {
                 const wNameStripped = stripPrefix(foundWard.name);
                 const subParts = result.dia_chi_duong.split(',').map(s => s.trim());
                 const lastSub = subParts[subParts.length - 1];
                  if (normalizeString(lastSub).includes(wNameStripped)) {
                     result.dia_chi_duong = subParts.slice(0, -1).join(', ');
                 }
             }
        } else {
             result.dia_chi_duong = fullAddress;
        }

        // 3. Try to convert using tinhthanhpho.com API
        if (API_KEY && foundProvince && foundDistrict && foundWard) {
            try {
                const payload = {
                    provinceCode: formatCode(foundProvince.code, 2),
                    districtCode: formatCode(foundDistrict.code, 3),
                    wardCode: formatCode(foundWard.code, 5),
                    streetAddress: result.dia_chi_duong
                };
                
                console.log('Calling conversion API with:', payload);

                const convertResponse = await axios.post(
                    `${TINH_THANH_PHO_API_URL}/convert/address`,
                    payload,
                    {
                        headers: {
                            'Authorization': `Bearer ${API_KEY}`,
                            'Content-Type': 'application/json'
                        }
                    }
                );

                if (convertResponse.data && convertResponse.data.success && convertResponse.data.data.new) {
                    const newData = convertResponse.data.data.new;
                    console.log('Converted to NEW address:', newData);
                    
                    // Normalize codes to match local unpadded format (e.g. "01" -> "1")
                    result.tinh_code = String(Number(newData.province.code));
                    result.tinh_name = newData.province.name;
                    result.xa_code = String(Number(newData.ward.code));
                    result.xa_name = newData.ward.name;
                    // result.dia_chi_duong = newData.fullAddress; 
                }
            } catch (apiErr) {
                console.warn('Failed to convert address with tinhthanhpho.com (using local parse):', apiErr);
            }
        } else if (!API_KEY) {
            console.warn('Skipping address conversion: No API Key provided for tinhthanhpho.com');
        }

        return result;

    } catch (error) {
        console.error('Error converting address:', error);
        return null;
    }
}