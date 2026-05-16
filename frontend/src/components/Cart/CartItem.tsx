import { CartItemResponse } from '../../types/cart';
import { formatPrice } from '../../utils/priceCalculation';
import { useState, useEffect } from 'react';
import { productService } from '../../services/api/productService';

interface CartItemProps {
  item: CartItemResponse;
  onRemove: () => void;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  onUpdateQuantity: (quantity: number) => void;
}

const CartItem = ({ item, onRemove, onUpdateQuantity }: CartItemProps) => {
  const [quantity, setQuantity] = useState(item.quantity);
  const [price, setPrice] = useState(0);
  const [productName, setProductName] = useState('');
  const [productImageUrl, setProductImageUrl] = useState<string | undefined>(undefined);
  const [availableStock, setAvailableStock] = useState<number>(0);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const product = await productService.getProductById(item.productId);
        setProductName(product.name);
        setPrice(product.price);
        setProductImageUrl(product.imageUrl);
        
        const stock = await productService.getAvailableStock(item.productId);
        setAvailableStock(stock);
      } catch (error) {
        console.error('Error fetching product:', error);
      }
    };

    fetchProduct();
  }, [item.productId]);

  const handleQuantityChange = (newQuantity: number) => {
    const validQuantity = Math.min(Math.max(1, newQuantity), availableStock);
    if (validQuantity !== quantity) {
      setQuantity(validQuantity);
      onUpdateQuantity(validQuantity);
    }
  };

  return (
    <div className="flex items-center gap-4 py-4 border-b last:border-b-0" data-testid="cart-item">
      <div className="w-16 h-16 bg-gray-200 rounded overflow-hidden flex-shrink-0 flex items-center justify-center">
        {productImageUrl ? (
          <img
            src={productImageUrl}
            alt={productName}
            className="w-full h-full object-cover"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none';
              if (e.target && (e.target as any).nextElementSibling) {
                 (e.target as any).nextElementSibling.style.display = 'block';
              }
            }}
          />
        ) : null}
        <span className={`text-gray-400 text-xs text-center ${productImageUrl ? 'hidden' : 'block'}`}>
          No Image
        </span>
      </div>
      <div className="flex-1">
        <h3 className="font-bold">{productName}</h3>
        <p className="text-gray-600">Mã sản phẩm: {item.productId}</p>
      </div>
      <div className="flex items-center gap-2">
        <button
          onClick={() => handleQuantityChange(quantity - 1)}
          disabled={quantity <= 1}
          className="px-2 py-1 border rounded hover:bg-gray-200 disabled:bg-gray-100 disabled:text-gray-400"
        >
          -
        </button>
        <input
          type="number"
          min="1"
          max={availableStock}
          value={quantity}
          onChange={(e) => handleQuantityChange(parseInt(e.target.value) || 1)}
          className="w-16 text-center border rounded py-1"
        />
        <button
          onClick={() => handleQuantityChange(quantity + 1)}
          disabled={quantity >= availableStock}
          className="px-2 py-1 border rounded hover:bg-gray-200 disabled:bg-gray-100 disabled:text-gray-400"
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
