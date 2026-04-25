import axios from "axios";

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
  try {
    const response = await axios.post<LoginResponse>(
      "http://localhost:8080/api/auth/login",
      {
        username,
        password,
      },
    );
    if (response.status === 200) {
      console.log("Login successful:", response.data);
      return response.data;
    }
    throw new Error("Login failed with status " + response.status);
  } catch (error) {
    throw error;
  }
}
