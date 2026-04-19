import { useState } from 'react';
import { Product } from '../../types/product';
import { useCart } from '../../hooks/useCart';
import { formatPrice } from '../../utils/priceCalculation';

interface ProductCardProps {
  product: Product;
}

const ProductCard = ({ product }: ProductCardProps) => {
  const userId = localStorage.getItem('userId') || 'user1';
  const { addItem } = useCart(userId);
  const [quantity, setQuantity] = useState(1);
  const [isAdding, setIsAdding] = useState(false);

  const handleAddToCart = async () => {
    setIsAdding(true);
    try {
      await addItem({
        productId: product.id,
        quantity,
      });
      setQuantity(1);
    } catch (error) {
      console.error('Error adding to cart:', error);
    } finally {
      setIsAdding(false);
    }
  };

  return (
    <div className="border rounded-lg overflow-hidden shadow-md hover:shadow-lg transition">
      <div className="bg-gray-200 h-48 flex items-center justify-center">
        <span className="text-gray-500">Hình ảnh</span>
      </div>
      <div className="p-4">
        <h2 className="font-bold text-lg mb-2">{product.name}</h2>
        <p className="text-gray-600 text-sm mb-4">{product.description}</p>
        <div className="flex justify-between items-center mb-4">
          <span className="text-xl font-bold text-blue-600">{formatPrice(product.price)}</span>
          <span className={`text-sm px-2 py-1 rounded ${
            product.status === 'ACTIVE' 
              ? 'bg-green-100 text-green-800' 
              : 'bg-red-100 text-red-800'
          }`}>
            {product.status === 'ACTIVE' ? 'Có sẵn' : 'Hết hàng'}
          </span>
        </div>
        <div className="flex gap-2">
          <input
            type="number"
            min="1"
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
            className="w-16 px-2 py-1 border rounded"
            disabled={product.status !== 'ACTIVE'}
          />
          <button
            onClick={handleAddToCart}
            disabled={isAdding || product.status !== 'ACTIVE'}
            className="flex-1 bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
          >
            {isAdding ? 'Đang thêm...' : 'Thêm vào giỏ'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
