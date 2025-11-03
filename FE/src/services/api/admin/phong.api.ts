import type { AxiosResponse } from "axios";
import request from "@/services/request";
import { PREFIX_API_PHONG_ADMIN } from "@/constants/url";
import type { PaginationParams, DefaultResponse, PaginationResponse } from "@/types/api.common";

export interface ParamsGetPhong extends PaginationParams {
  q?: string;
  loaiPhong?: string | null;
  trangThaiPhong?: number | null;
  giaMin?: number | null;
  giaMax?: number | null;
  sucChuaMin?: number | null;
  sucChuaMax?: number | null;
}

export type PhongResponse = {
  id: string;
  maPhong: string;
  tenPhong: string;
  loaiPhong: string;
  giaHienTai: number;
  sucChua: number;
  trangThaiPhong: number;
};

export const getRooms = async (params: ParamsGetPhong) => {
  const res = (await request({
    url: `${PREFIX_API_PHONG_ADMIN}`,
    method: "GET",
    params,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhongResponse>>>>;

  return res.data;
};
