// src/components/Inventory/Inventory.tsx

import { useEffect, useState } from "react";
import { inventoryService } from "../../services/api/inventoryService";
import { InventoryItem } from "../../types/inventory";

const Inventory = () => {
  const productId = localStorage.getItem("productId") || "";
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getInventoryItems = async () => {
    if (!productId) {
      setError("Product ID not found");
      return;
    }

    setIsLoading(true);
    try {
      const response = await inventoryService.getInventoryItems(productId);
      setInventoryItems(response);
      setError(null);
    } catch (err) {
      setError((err as Error).message || "Failed to load inventory");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getInventoryItems();
  }, [productId]); // ← Thêm dependency array

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  if (inventoryItems.length === 0) {
    return <div>No inventory items found</div>; // ← Empty state
  }

  return (
    <div>
      <h1>Inventory</h1>
      {inventoryItems.map((inventoryItem) => (
        <div key={inventoryItem.id}>
          <p>Product ID: {inventoryItem.productId}</p>
          <p>Quantity: {inventoryItem.quantity}</p>
        </div>
      ))}
    </div>
  );
};
export default Inventory;
