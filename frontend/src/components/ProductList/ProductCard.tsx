import { useState, useEffect } from "react";
import { Product } from "../../types/product";
import { useCart } from "../../hooks/useCart";
import { formatPrice } from "../../utils/priceCalculation";
import { productService } from "../../services/api/productService";

interface ProductCardProps {
  product: Product;
}

const ProductCard = ({ product }: ProductCardProps) => {
  const { cart, addItem } = useCart();
  const [quantity, setQuantity] = useState(1);
  const [isAdding, setIsAdding] = useState(false);
  const [availableStock, setAvailableStock] = useState<number>(0);

  // Tính số lượng đang có trong giỏ hàng
  const quantityInCart = cart?.items.find((item) => item.productId === product.id)?.quantity || 0;
  // Số lượng thực sự có thể thêm vào giỏ
  const displayStock = Math.max(0, availableStock - quantityInCart);

  useEffect(() => {
    const fetchStock = async () => {
      try {
        const stock = await productService.getAvailableStock(product.id);
        setAvailableStock(stock);
      } catch (error) {
        console.error("Error fetching stock:", error);
      }
    };
    if (product.status === "ACTIVE") {
      fetchStock();
    }
  }, [product.id, product.status]);

  const handleAddToCart = async () => {
    setIsAdding(true);
    try {
      await addItem({
        productId: product.id,
        quantity,
      });
      setQuantity(1);
      alert("Đã thêm vào giỏ hàng thành công!");
    } catch (error: any) {
      console.error("Error adding to cart:", error);
      alert(error.message || "Có lỗi xảy ra khi thêm vào giỏ hàng.");
    } finally {
      setIsAdding(false);
    }
  };

  return (
    <div className="border rounded-lg overflow-hidden shadow-md hover:shadow-lg transition">
      <div className="bg-gray-200 h-48 flex items-center justify-center">
        <span className="text-gray-500">Id sản phẩm {product.id}</span>
      </div>
      <div className="p-4">
        <h2 className="font-bold text-lg mb-2">{product.name}</h2>
        <p className="text-gray-600 text-sm mb-4">{product.description}</p>
        <div className="flex justify-between items-center mb-4">
          <span className="text-xl font-bold text-blue-600">
            {formatPrice(product.price)}
          </span>
          <span
            className={`text-sm px-2 py-1 rounded ${
              product.status === "ACTIVE" && displayStock > 0
                ? "bg-green-100 text-green-800"
                : "bg-red-100 text-red-800"
            }`}
          >
            {product.status === "ACTIVE" && displayStock > 0 ? `Có sẵn (${displayStock})` : "Hết hàng"}
          </span>
        </div>
        <div className="flex gap-2">
          <input
            type="number"
            min="1"
            max={displayStock}
            value={quantity}
            onChange={(e) => {
              const val = Math.max(1, parseInt(e.target.value) || 1);
              setQuantity(Math.min(val, displayStock));
            }}
            className="w-16 px-2 py-1 border rounded"
            disabled={product.status !== "ACTIVE" || displayStock === 0}
          />
          <button
            onClick={handleAddToCart}
            disabled={isAdding || product.status !== "ACTIVE" || displayStock === 0}
            className="flex-1 bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
          >
            {isAdding ? "Đang thêm..." : "Thêm vào giỏ"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
