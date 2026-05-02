package com.shopcart.service;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import com.shopcart.dto.request.CartItemRequest;
import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.mapper.CartMapper;
import com.shopcart.repository.CartRepository;
import com.shopcart.service.impl.CartServiceImpl;
import com.shopcart.service.impl.InventoryServiceImpl;  
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@DisplayName("Cart Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private IProductService productService;

    @Mock
    private IInventoryService inventoryService;

    @InjectMocks
    private CartServiceImpl cartService;

    private String userId;
    private CartItemRequest request;
   
    @BeforeEach
    void setUp() {
        userId = "user-1";
        request = CartItemRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .build();
    }

    @Nested
    @DisplayName("1. Test cases cho hàm addToCart")
    class addToCart {

    @DisplayName("TC1: Thêm sản phẩm mới vào giỏ hàng thành công")
    @Test
        void addToCart_NewProduct_Success() {
        // 1. Arrange
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());

        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                .thenReturn(Optional.empty());

        when(inventoryService.hasEnoughStock(request.getProductId(), request.getQuantity()))
                .thenReturn(true);

        mockBuildCartResponseHelper(List.of(new CartItem()));

        // 2. Act
        CartResponse response = cartService.addToCart(userId, request);

        // 3. Assert
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(userId, savedCartItem.getUserId());
        assertEquals(request.getProductId(), savedCartItem.getProductId());
        assertEquals(request.getQuantity(), savedCartItem.getQuantity());
        
        assertNotNull(response); 
    }
    @DisplayName("TC2: Thêm sản phẩm đã có trong giỏ")
    @Test
    void addToCart_ExistingProduct_Success(){
        // 1. Arrange
        int existingQuantity = 3;
        int expectedNewQuantity = request.getQuantity() + existingQuantity;
        // Giả lập sản phẩm đã có trong giỏ
        CartItem existingCartItem = CartItem.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .quantity(existingQuantity)
                    .build();
        
        // Giả lập sản phẩm hợp lệ
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());

            // Giả lập repository trả về sản phẩm ĐÃ TỒN TẠI 
        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                    .thenReturn(Optional.of(existingCartItem));

            // Mock kiểm tra tồn kho phải dùng TỔNG SỐ LƯỢNG MỚI (expectedNewQuantity)
        when(inventoryService.hasEnoughStock(request.getProductId(), expectedNewQuantity))
                    .thenReturn(true);

        mockBuildCartResponseHelper(List.of(new CartItem()));

        // 2. Act
        CartResponse response = cartService.addToCart(userId, request);

        // 3. Assert

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
            
            verify(cartRepository, times(1)).save(cartItemCaptor.capture());
            CartItem savedCartItem = cartItemCaptor.getValue();
            
            assertEquals(userId, savedCartItem.getUserId());
            assertEquals(request.getProductId(), savedCartItem.getProductId());
            assertEquals(expectedNewQuantity, savedCartItem.getQuantity()); 
            
            assertNotNull(response);
    }
    @DisplayName("TC3: Thêm sản phẩm với số lượng tồn kho không đủ")
    @Test
    void addToCart_InsufficientStock_ThrowsException() {
            // 1. Arrange
            // Giả lập sản phẩm tồn tại
            when(productService.getProductById(request.getProductId())).thenReturn(new Product());

            // Giả lập sản phẩm chưa có trong giỏ hàng
            when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                    .thenReturn(Optional.empty());

            // Giả lập kho KHÔNG đủ số lượng (trả về false)
            when(inventoryService.hasEnoughStock(request.getProductId(), request.getQuantity()))
                    .thenReturn(false);

            // 2. Act 
            // Kiểm tra xem Service có ném đúng BusinessLogicException hay không
            BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                cartService.addToCart(userId, request);
            });

            // 3. Assert
            // Kiểm tra nội dung message trong exception có khớp không
            String expectedMessage = "Insufficient stock for product: " + request.getProductId();
            assertEquals(expectedMessage, exception.getMessage());

            // Đảm bảo hàm save() KHÔNG bao giờ được gọi khi có lỗi
            verify(cartRepository, never()).save(any(CartItem.class));
            
            // Đảm bảo mapper cũng không được gọi 
            verify(cartMapper, never()).toCartResponse(anyString(), anyList());
    }
    @DisplayName("TC4: Thêm sản phẩm không tồn tại vào giỏ")
    @Test
    void addToCart_ProductNotFound_ThrowsException() {
            // 1. Arrange
            // Giả lập ProductService ném ra exception khi tìm kiếm product id này
            when(productService.getProductById(request.getProductId()))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            // 2. Act & Assert
            // Kiểm chứng xem hàm addToCart có quăng đúng lỗi ResourceNotFoundException ra không
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                cartService.addToCart(userId, request);
            });

            // 3. Verify 
            // Đảm bảo rằng luồng code đã dừng lại ngay lập tức và KHÔNG CÓ hàm nào bên dưới được gọi
            verify(cartRepository, never()).findByUserIdAndProductId(anyString(), anyString());
            verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
            verify(cartRepository, never()).save(any(CartItem.class));
    }

    }


    @Nested
    @DisplayName("Tests cho hàm removeFromCart")
         class RemoveFromCart {

        private Long cartItemId;

        @BeforeEach
        void setUpRemove() {
            cartItemId = 999L; 
        }

        @Test
        @DisplayName("TC1: Xóa cart item thành công")
        void removeFromCart_Success() {
            // 1. Arrange
            CartItem validCartItem = CartItem.builder()
                    .userId(userId) // Trùng khớp với userId đang request
                    .productId("PROD-001")
                    .quantity(2)
                    .build();

            when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
            
            mockBuildCartResponseHelper(List.of()); // Truyền list rỗng vì item đã bị xóa

            // 2. Act
            CartResponse response = cartService.removeFromCart(userId, cartItemId);

            // 3. Assert
            // Đảm bảo hàm delete(cartItem) được gọi ĐÚNG 1 LẦN với đúng đối tượng đó
            verify(cartRepository, times(1)).delete(validCartItem);
            assertNotNull(response);
        }

        @Test
        @DisplayName("TC2: Xóa thất bại do không tìm thấy ID trong DB")
        void removeFromCart_ItemNotFound_ThrowsException() {
            // 1. Arrange
            when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

            // 2. Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                cartService.removeFromCart(userId, cartItemId);
            });

            assertEquals("Không tìm thấy cart item với id: " + cartItemId, exception.getMessage());

            // 3. Verify: Tuyệt đối hàm delete KHÔNG bao giờ được gọi
            verify(cartRepository, never()).delete(any(CartItem.class));
        }

        @Test
        @DisplayName("TC3: Xóa thất bại do Item thuộc về User khác (Bảo mật IDOR)")
        void removeFromCart_WrongUser_ThrowsException() {
            // 1. Arrange
            String hackerId = "hacker-999";
            
            // Giả lập item có tồn tại, nhưng userId lại là của người khác
            CartItem someoneElsesItem = CartItem.builder()
                    .userId(hackerId) 
                    .productId("PROD-001")
                    .quantity(1)
                    .build();

            when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(someoneElsesItem));

            // 2. Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                cartService.removeFromCart(userId, cartItemId);
            });

            assertEquals("Cart item không thuộc về user: " + userId, exception.getMessage());

            // 3. Verify : Không bao giờ được phép xóa
            verify(cartRepository, never()).delete(any(CartItem.class));
        }
    }
    @Nested
    @DisplayName("Tests cho hàm updateQuantity")
    class UpdateQuantity {

        private Long cartItemId;
        private Integer newQuantity;

        @BeforeEach
        void setUpUpdate() {
            cartItemId = 999L;
            newQuantity = 10; 
        }

        @Test
        @DisplayName("TC1: Cập nhật số lượng thành công")
        void updateQuantity_Success() {
            // 1. Arrange
            CartItem validCartItem = CartItem.builder()
                    .userId(userId)
                    .productId("PROD-001")
                    .quantity(2) 
                    .build();

            when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
            
            when(inventoryService.hasEnoughStock(validCartItem.getProductId(), newQuantity))
                    .thenReturn(true);

            mockBuildCartResponseHelper(List.of(validCartItem));

            // 2. Act
            CartResponse response = cartService.updateQuantity(userId, cartItemId, newQuantity);

            // 3. Assert
            ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartRepository, times(1)).save(cartItemCaptor.capture());

            CartItem savedCartItem = cartItemCaptor.getValue();
            assertEquals(newQuantity, savedCartItem.getQuantity());
            assertNotNull(response);
        }

        @Test
        @DisplayName("TC2: Cập nhật thất bại do kho không đủ hàng")
        void updateQuantity_InsufficientStock_ThrowsException() {
            // 1. Arrange
            CartItem validCartItem = CartItem.builder()
                    .userId(userId)
                    .productId("PROD-001")
                    .quantity(2)
                    .build();

            when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
            
            when(inventoryService.hasEnoughStock(validCartItem.getProductId(), newQuantity))
                    .thenReturn(false);

            // 2. Act & Assert
            BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                cartService.updateQuantity(userId, cartItemId, newQuantity);
            });

            assertEquals("Insufficient stock for product: " + validCartItem.getProductId(), exception.getMessage());

            verify(cartRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("TC3: Thất bại do Cart Item không tồn tại")
        void updateQuantity_ItemNotFound_ThrowsException() {
            // 1. Arrange
            when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

            // 2. Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                cartService.updateQuantity(userId, cartItemId, newQuantity);
            });

            assertEquals("Không tìm thấy cart item với id: " + cartItemId, exception.getMessage());
            
            verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
            verify(cartRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("TC4: Thất bại do Item thuộc về User khác (Bảo mật IDOR)")
        void updateQuantity_WrongUser_ThrowsException() {
            // 1. Arrange
            String hackerId = "hacker-999";
            CartItem someoneElsesItem = CartItem.builder()
                    .userId(hackerId)
                    .productId("PROD-001")
                    .quantity(1)
                    .build();

            when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(someoneElsesItem));

            // 2. Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                cartService.updateQuantity(userId, cartItemId, newQuantity);
            });

            assertEquals("Cart item không thuộc về user: " + userId, exception.getMessage());
            
            verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
            verify(cartRepository, never()).save(any(CartItem.class));
        }
    }

    @Nested
    @DisplayName("Tests cho hàm getCart")
        class GetCart {

        @Test
        @DisplayName("TC1: Lấy giỏ hàng thành công khi có sản phẩm")
        void getCart_WithItems_Success() {
            // 1. Arrange
            CartItem item1 = CartItem.builder().userId(userId).productId("PROD-001").quantity(1).build();
            CartItem item2 = CartItem.builder().userId(userId).productId("PROD-002").quantity(3).build();
            List<CartItem> mockCartItems = List.of(item1, item2);

            // Giả lập DB trả về danh sách 2 sản phẩm này
            when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(mockCartItems);

            // Giả lập Mapper biến danh sách thành CartResponse
            CartResponse mockResponse = new CartResponse(); 
            when(cartMapper.toCartResponse(userId, mockCartItems))
                    .thenReturn(mockResponse);

            // 2. Act
            CartResponse actualResponse = cartService.getCart(userId);

            // 3. Assert
            assertNotNull(actualResponse);
            assertEquals(mockResponse, actualResponse); // Đảm bảo trả về đúng cái response mà mapper đã build
            
            // Đảm bảo các dependency được gọi đúng 1 lần
            verify(cartRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
            verify(cartMapper, times(1)).toCartResponse(userId, mockCartItems);
        }

        @Test
        @DisplayName("TC2: Lấy giỏ hàng thành công khi giỏ hàng trống")
        void getCart_EmptyCart_Success() {
            // 1. Arrange
            // Giỏ hàng trống -> DB trả về list rỗng
            List<CartItem> emptyCartItems = List.of(); 

            when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(emptyCartItems);

            CartResponse mockEmptyResponse = new CartResponse();
            when(cartMapper.toCartResponse(userId, emptyCartItems))
                    .thenReturn(mockEmptyResponse);

            // 2. Act
            CartResponse actualResponse = cartService.getCart(userId);

            // 3. Assert
            assertNotNull(actualResponse);
            assertEquals(mockEmptyResponse, actualResponse);
            
            verify(cartRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
            verify(cartMapper, times(1)).toCartResponse(userId, emptyCartItems);
        }
    }
    @Nested
    @DisplayName("Tests cho hàm clearCart")
    class ClearCart {

        @Test
        @DisplayName("TC1: Xóa toàn bộ giỏ hàng thành công")
        void clearCart_Success() {
            // Đối với hàm void như deleteByUserId, Mockito mặc định không làm gì cả.
            // 1. Act
            cartService.clearCart(userId);
            // 2. Assert
            verify(cartRepository, times(1)).deleteByUserId(userId);
        }
    }




    // Mock helper build response
    public void mockBuildCartResponseHelper(List<CartItem> mockCartItems) {
        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(mockCartItems);
        when(cartMapper.toCartResponse(userId, mockCartItems))
                .thenReturn(new CartResponse());
    }

    }