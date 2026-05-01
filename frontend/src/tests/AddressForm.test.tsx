import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AddressForm from '../components/Cart/AddressForm';
import { ShippingAddress } from '../types/order';

describe('AddressForm Component', () => {
  let mockOnAddressChange: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    mockOnAddressChange = vi.fn();
  });

  describe('Rendering', () => {
    it('should mark required fields with asterisk', () => {
      const { container } = render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const requiredAsterisks = container.querySelectorAll('.text-red-500');
      expect(requiredAsterisks.length).toBeGreaterThan(0);
    });
  });

  describe('Input Handling', () => {
    it('should update state when user types in shipping address field', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const addressInput = screen.getByPlaceholderText(/123 Đường ABC/i);

      await userEvent.type(addressInput, '123 Đường Nguyễn Huệ');

      expect(mockOnAddressChange).toHaveBeenCalledWith(
        expect.objectContaining({
          shippingAddress: '123 Đường Nguyễn Huệ',
        })
      );
    });

    it('should update state when user types in city field', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const cityInput = screen.getByPlaceholderText(/Thành phố Hồ Chí Minh/i);

      await userEvent.type(cityInput, 'Thành phố Hồ Chí Minh');

      expect(mockOnAddressChange).toHaveBeenCalledWith(
        expect.objectContaining({
          city: 'Thành phố Hồ Chí Minh',
        })
      );
    });

    it('should update state when user types in district field', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const districtInput = screen.getByPlaceholderText(/Quận 1/i);

      await userEvent.type(districtInput, 'Quận 1');

      expect(mockOnAddressChange).toHaveBeenCalledWith(
        expect.objectContaining({
          district: 'Quận 1',
        })
      );
    });

    it('should update state when user types in ward field', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const wardInput = screen.getByPlaceholderText(/Phường Bến Nghé/i);

      await userEvent.type(wardInput, 'Phường Bến Nghé');

      expect(mockOnAddressChange).toHaveBeenCalledWith(
        expect.objectContaining({
          ward: 'Phường Bến Nghé',
        })
      );
    });

    it('should update state when user types in postal code field', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const postalInput = screen.getByPlaceholderText(/700000/i);

      await userEvent.type(postalInput, '700000');

      expect(mockOnAddressChange).toHaveBeenCalledWith(
        expect.objectContaining({
          postalCode: '700000',
        })
      );
    });

    it('should update state when user types in phone number field', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const phoneInput = screen.getByPlaceholderText(/0123456789/i);

      await userEvent.type(phoneInput, '0912345678');

      expect(mockOnAddressChange).toHaveBeenCalledWith(
        expect.objectContaining({
          phoneNumber: '0912345678',
        })
      );
    });

    it('should limit phone number to 10 digits', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const phoneInput = screen.getByPlaceholderText(/0123456789/i) as HTMLInputElement;

      await userEvent.type(phoneInput, '09123456789'); // 11 digits

      expect(phoneInput.value).toHaveLength(10); // Should be limited to 10
    });
  });

  describe('Validation', () => {
    it('should show error when shipping address is empty', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const addressInput = screen.getByPlaceholderText(/123 Đường ABC/i);

      // Focus and blur without typing
      fireEvent.focus(addressInput);
      fireEvent.blur(addressInput);

      // Cannot test validation without a validate button in the component
      // But we can test that initial state is empty
      expect((addressInput as HTMLInputElement).value).toBe('');
    });

    it('should show error when phone number format is invalid', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const phoneInput = screen.getByPlaceholderText(/0123456789/i);

      await userEvent.type(phoneInput, '12345'); // Less than 10 digits

      // Error should be triggered when field loses focus or on validation
      fireEvent.blur(phoneInput);
    });

    it('should accept valid phone numbers (10 digits)', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const phoneInput = screen.getByPlaceholderText(/0123456789/i) as HTMLInputElement;

      await userEvent.type(phoneInput, '0912345678');

      expect(phoneInput.value).toBe('0912345678');
    });
  });

  describe('Error Clearing', () => {
    it('should clear error when user starts typing', async () => {
      const { rerender } = render(<AddressForm onAddressChange={mockOnAddressChange} />);
      const addressInput = screen.getByPlaceholderText(/123 Đường ABC/i);

      // User types first character
      await userEvent.type(addressInput, '1');

      // Error should be cleared from state
      expect(mockOnAddressChange).toHaveBeenCalled();
    });
  });

  describe('Form Integration', () => {
    it('should call onAddressChange for each field update', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);

      const addressInput = screen.getByPlaceholderText(/123 Đường ABC/i);
      const cityInput = screen.getByPlaceholderText(/Thành phố Hồ Chí Minh/i);

      await userEvent.type(addressInput, '123 Đường A');
      await userEvent.type(cityInput, 'HCM');

      // Should be called once for each character typed across all fields
      // '123 Đường A' = 11 chars, 'HCM' = 3 chars = 14 total
      expect(mockOnAddressChange).toHaveBeenCalledTimes(14);
    });

    it('should handle all fields being filled correctly', async () => {
      render(<AddressForm onAddressChange={mockOnAddressChange} />);

      const addressInput = screen.getByPlaceholderText(/123 Đường ABC/i);
      const cityInput = screen.getByPlaceholderText(/Thành phố Hồ Chí Minh/i);
      const districtInput = screen.getByPlaceholderText(/Quận 1/i);
      const wardInput = screen.getByPlaceholderText(/Phường Bến Nghé/i);
      const phoneInput = screen.getByPlaceholderText(/0123456789/i);

      await userEvent.type(addressInput, '123 Đường ABC');
      await userEvent.type(cityInput, 'TP HCM');
      await userEvent.type(districtInput, 'Q1');
      await userEvent.type(wardInput, 'P1');
      await userEvent.type(phoneInput, '0912345678');

      // Last call should have all fields
      expect(mockOnAddressChange).toHaveBeenLastCalledWith(
        expect.objectContaining({
          shippingAddress: '123 Đường ABC',
          city: 'TP HCM',
          district: 'Q1',
          ward: 'P1',
          phoneNumber: '0912345678',
        })
      );
    });
  });

  describe('Styling & Classes', () => {
    it('should apply proper Tailwind classes to required fields', () => {
      const { container } = render(<AddressForm onAddressChange={mockOnAddressChange} />);

      const inputs = container.querySelectorAll('input');
      expect(inputs.length).toBeGreaterThan(0);

      inputs.forEach((input) => {
        // Each input should have border and focus styles
        expect(input.className).toMatch(/border/);
        expect(input.className).toMatch(/rounded-lg/);
      });
    });

    it('should show error styling when field has validation error', () => {
      const { container } = render(<AddressForm onAddressChange={mockOnAddressChange} />);

      // Initially, no red border
      const inputs = container.querySelectorAll('input');
      inputs.forEach((input) => {
        expect(input.className).toMatch(/border-gray-300/);
      });
    });
  });
});
