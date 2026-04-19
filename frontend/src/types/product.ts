export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface ProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  status: string;
}
