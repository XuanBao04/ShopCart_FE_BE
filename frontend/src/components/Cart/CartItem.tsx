import { CartItemResponse } from '../../types/cart';
import { formatPrice } from '../../utils/priceCalculation';
import { useState, useEffect } from 'react';
import { productService } from '../../services/api/productService';

interface CartItemProps {
  item: CartItemResponse;
  onRemove: () => void;
  onUpdateQuantity: (quantity: number) => void;
}

const CartItem = ({ item, onRemove, onUpdateQuantity }: CartItemProps) => {
  const [quantity, setQuantity] = useState(item.quantity);
  const [price, setPrice] = useState(0);
  const [productName, setProductName] = useState('');

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const product = await productService.getProductById(item.productId);
        setProductName(product.name);
        setPrice(product.price);
      } catch (error) {
        console.error('Error fetching product:', error);
      }
    };

    fetchProduct();
  }, [item.productId]);

  const handleQuantityChange = (newQuantity: number) => {
    if (newQuantity >= 1) {
      setQuantity(newQuantity);
      onUpdateQuantity(newQuantity);
    }
  };

  return (
    <div className="flex items-center gap-4 py-4 border-b last:border-b-0">
      <div className="flex-1">
        <h3 className="font-bold">{productName}</h3>
        <p className="text-gray-600">Mã sản phẩm: {item.productId}</p>
      </div>
      <div className="flex items-center gap-2">
        <button
          onClick={() => handleQuantityChange(quantity - 1)}
          className="px-2 py-1 border rounded hover:bg-gray-200"
        >
          -
        </button>
        <input
          type="number"
          value={quantity}
          onChange={(e) => handleQuantityChange(Math.max(1, parseInt(e.target.value) || 1))}
          className="w-12 text-center border rounded py-1"
        />
        <button
          onClick={() => handleQuantityChange(quantity + 1)}
          className="px-2 py-1 border rounded hover:bg-gray-200"
        >
          +
        </button>
      </div>
      <div className="text-right w-24">
        <p className="font-bold">{formatPrice(price * quantity)}</p>
        <p className="text-gray-600 text-sm">{formatPrice(price)} x {quantity}</p>
      </div>
      <button
        onClick={onRemove}
        className="text-red-600 hover:text-red-800 font-bold"
      >
        Xóa
      </button>
    </div>
  );
};

export default CartItem;
