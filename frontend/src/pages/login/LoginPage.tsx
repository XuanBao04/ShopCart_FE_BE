import { useEffect, useState } from "react";
import { loginService } from "@/services/api/loginService";
import { useNavigate } from "react-router-dom";

const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  
  useEffect(() => {
    const storedUserId = localStorage.getItem("userId");
    if (storedUserId) {
      navigate("/authenticated/products", { replace: true });
    }
  }, [navigate]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!username || !password) {
      setError("Vui lòng nhập đầy đủ thông tin đăng nhập.");
      return;
    }

    try {
      const response = await loginService(username, password);
      if (response) {
        console.log(response);
        localStorage.setItem("userId", response.userId.toString());
        navigate("/authenticated/products", { replace: true });
      }
    } catch (error) {
      throw error;
    }

    setError(null);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">Đăng nhập</h1>

        {error && (
          <div className="bg-red-100 text-red-700 p-4 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-sm font-bold mb-1">
              Tên đăng nhập
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full border rounded px-3 py-2"
              placeholder="Nhập tên đăng nhập"
              name="username"
              autoComplete="username"
            />
          </div>

          <div>
            <label className="block text-sm font-bold mb-1">Mật khẩu</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border rounded px-3 py-2"
              placeholder="Nhập mật khẩu"
              name="password"
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
          >
            Đăng nhập
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
