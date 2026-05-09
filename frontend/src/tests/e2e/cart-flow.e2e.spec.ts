import { expect, test, type Locator, type Page } from '@playwright/test';

const E2E_USER_ID = '1';

/**
 * Dang nhap gia lap bang localStorage de vao duoc cart flow.
 * Test nay KHONG mock API, nen moi request se goi backend that.
 */
async function setupAuthenticatedSession(page: Page) {
  await page.addInitScript((userId) => {
    window.localStorage.setItem('userId', userId);
    window.localStorage.setItem('role', 'USER');
    window.localStorage.setItem('username', 'e2e-user');
  }, E2E_USER_ID);
}

/**
 * Tim card san pham dau tien con hang (input so luong dang enabled).
 * Neu khong tim thay, test se skip de tranh fail gia do du lieu backend khong phu hop.
 */
async function getFirstAvailableProductCard(page: Page): Promise<Locator | null> {
  const cards = page.locator('div.border.rounded-lg.overflow-hidden.shadow-md.hover\\:shadow-lg.transition');
  const count = await cards.count();

  for (let i = 0; i < count; i += 1) {
    const card = cards.nth(i);
    const quantityInput = card.locator('input[type="number"]');
    if (await quantityInput.isEnabled()) {
      return card;
    }
  }

  return null;
}

test.describe('Cart E2E - Add to cart voi backend that (khong mock API)', () => {
  test.beforeEach(async ({ page }) => {
    await setupAuthenticatedSession(page);
  });

  test('1) Complete add-to-cart flow: them san pham, mo gio hang, tang so luong, xoa item', async ({ page }) => {
    await page.goto('/authenticated/products');

    const productCard = await getFirstAvailableProductCard(page);
    test.skip(!productCard, 'Khong co san pham con hang tren backend de thuc hien test add-to-cart.');

    const productName = (await productCard!.locator('h2').innerText()).trim();

    // Buoc 1: Them san pham vao gio hang tai trang danh sach san pham.
    await productCard!.getByRole('button', { name: /th.+m v.+o gi.+ h.+ng/i }).click();

    // Buoc 2: Verify toast thanh cong xuat hien.
    await expect(page.getByText(/th.+m s.+n ph.+m v.+o gi.+ h.+ng/i)).toBeVisible();

    // Buoc 3: Mo trang gio hang va verify san pham vua them co trong cart.
    await page.locator('header button.bg-green-500').click();
    await expect(page.getByText(productName)).toBeVisible();

    // Buoc 4: Tang so luong san pham trong gio (nut +) va verify input quantity cap nhat.
    const quantityInput = page.locator('input[type="number"]').first();
    const currentValue = Number(await quantityInput.inputValue());
    await page.getByRole('button', { name: '+' }).first().click();
    await expect(quantityInput).toHaveValue(String(currentValue + 1));

    // Buoc 5: Xoa item khoi gio hang de ket thuc flow add-to-cart.
    await page.getByRole('button', { name: /^x.+a$/i }).first().click();
    await expect(page.getByText(/gi.+ h.+ng tr.+ng/i)).toBeVisible();
  });

  test('2) Validation ton kho o UI: input so luong khong duoc vuot qua max stock hien tai', async ({ page }) => {
    await page.goto('/authenticated/products');

    const productCard = await getFirstAvailableProductCard(page);
    test.skip(!productCard, 'Khong co san pham con hang tren backend de test validation ton kho.');

    const quantityInput = productCard!.locator('input[type="number"]');
    const maxAttr = await quantityInput.getAttribute('max');

    test.skip(!maxAttr || Number(maxAttr) < 1, 'Backend tra stock khong hop le de test validation.');

    // Buoc 1: Co tinh nhap mot so luong lon hon gioi han max hien thi tren UI.
    const maxStock = Number(maxAttr);
    await quantityInput.fill(String(maxStock + 99));

    // Buoc 2: Verify gia tri input bi clamp lai khong vuot qua max.
    // Day la validation quan trong o layer frontend truoc khi goi API them gio hang.
    await expect(quantityInput).toHaveValue(String(maxStock));
  });
});
