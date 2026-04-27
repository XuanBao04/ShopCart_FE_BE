import apiClient from "./apiClient";

export interface LoginResponse {
  userId: number;
  username: string;
  fullName: string;
  email: string;
  role: string;
  message: string;
}

export async function loginService(
  username: string,
  password: string,
): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>(
    "/auth/login",
    {
      username,
      password,
    },
  );
  if (response.status === 200) {
    return response.data;
  }
  throw new Error("Login failed with status " + response.status);
}
