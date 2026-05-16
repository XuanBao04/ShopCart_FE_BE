import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from "axios";


const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
const apiClient: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
});

const readCookie = (name: string): string | null => {
  const cookie = document.cookie
    .split(";")
    .map((c) => c.trim())
    .find((c) => c.startsWith(`${name}=`));

  return cookie ? decodeURIComponent(cookie.substring(name.length + 1)) : null;
};

const fetchCsrfToken = async (): Promise<string | null> => {
  await axios.get(`${API_URL}/csrf-token`, {
    withCredentials: true,
  });

  return readCookie("XSRF-TOKEN");
};

// Ensure CSRF cookie is present before mutating requests
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    // Add auth token if needed
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    const method = (config.method || "get").toString().toUpperCase();
    const needsCsrf = ["POST", "PUT", "PATCH", "DELETE"].includes(method);
    if (needsCsrf) {
      let csrfToken = readCookie("XSRF-TOKEN");
      if (!csrfToken) {
        try {
          csrfToken = await fetchCsrfToken();
        } catch {
          // ignore - request will likely fail later with CSRF error
        }
      }

      if (csrfToken) {
        config.headers["X-XSRF-TOKEN"] = csrfToken;
      }
    }

    return config;
  },
  (error) => Promise.reject(error),
);

// Response interceptor - redirect to login on 401
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("userId");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export default apiClient;
