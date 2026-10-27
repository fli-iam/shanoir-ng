import { test, expect } from '@playwright/test';

const randomString = Math.random().toString().substr(2,10);

test('admin-study-create-delete', async ({ page }) => {
  await page.goto(process.env.BASE_URL);

  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.ADMIN_NAME);
  await page.getByRole('textbox', { name: 'Username or email' }).press('Tab');
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.ADMIN_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.getByRole('textbox').first().click();
  await page.getByRole('textbox').first().fill('playwright-test-study-create-' + randomString);
  await page.getByRole('listitem').filter({ hasText: 'Start date' }).getByRole('textbox').fill('2026-10-01');
  await page.getByRole('listitem').filter({ hasText: 'End date' }).getByRole('textbox').fill('2026-10-31');
  // select pseudo profile
  const profileItem = page.getByRole('listitem').filter({ hasText: 'Pseudonymization profile' });
  await profileItem.getByPlaceholder('Select an option...').click();
  const option = page.locator('.list > div').filter({ hasText: 'Profile Neurinfo' });
  await expect(option).toBeVisible();
  await option.click();
  // disable study cards
  await page.getByRole('radio').nth(1).check();
  // add center to study
  await page.getByRole('listitem', { name: 'At least one center is' }).click();
  await page.getByRole('textbox', { name: 'Select a center' }).click();
  await page.getByText('CHU Rennes').click();
  await page.locator('.commands > span:nth-child(3) > .fas').click();
  await expect(page.locator('form')).toContainText('CHU Rennes');
  // save study
  await page.getByRole('button', { name: 'Save' }).click();
  await page.getByRole('button', { name: 'No' }).click();
  await expect(page.locator('study-node')).toContainText('playwright-test-study-create-' + randomString, { timeout: 10000 });
  await expect(page.getByRole('heading')).toContainText('Details on study', { timeout: 10000 });
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();

  await page.getByText('Logout').click();
});

test('expert-study-create-edit-delete', async ({ page }) => {
  await page.goto(process.env.BASE_URL);

  // role-expert: study draft creation and validation by admin
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.getByRole('textbox').first().click();
  await page.getByRole('textbox').first().fill('playwright-test-study-draft-create-' + randomString);
  await page.getByRole('listitem').filter({ hasText: 'Start date' }).getByRole('textbox').fill('2026-10-01');
  await page.getByRole('listitem').filter({ hasText: 'End date' }).getByRole('textbox').fill('2026-10-31');
  // select pseudo profile
  const profileItem = page.getByRole('listitem').filter({ hasText: 'Pseudonymization profile' });
  await profileItem.getByPlaceholder('Select an option...').click();
  const option = page.locator('.list > div').filter({ hasText: 'Profile Neurinfo' });
  await expect(option).toBeVisible();
  await option.click();
  // disable study cards
  await page.getByRole('radio').nth(1).check();
  // add center to study
  await page.getByRole('listitem', { name: 'At least one center is' }).click();
  await page.getByRole('textbox', { name: 'Select a center' }).click();
  await page.getByText('CHU Rennes').click();
  await page.locator('.commands > span:nth-child(3) > .fas').click();
  await expect(page.locator('form')).toContainText('CHU Rennes');
  await page.getByRole('button', { name: 'Save' }).click();
  await page.getByRole('button', { name: 'No' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on study');
  await page.getByText('Logout').click();
  await page.locator('div').nth(2).click();
  // role-admin: validate study draft
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.ADMIN_NAME);
  await page.getByRole('textbox', { name: 'Username or email' }).press('Tab');
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.ADMIN_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page.getByText(/Notifications\s*\d+/)).toBeVisible();
  await page.getByText(/Notifications\s*\d+/).click();
  await expect(page.getByText(/Draft\s*Studies\s*\d+/)).toBeVisible();
  await page.getByText(/Draft\s*Studies\s*\d+/).click();
  const studyRow = page
    .locator('form')
    .filter({ hasText: 'User ' + process.env.EXPERT_NAME + ' Study playwright-test-study-draft-create-' + randomString });
  await studyRow.getByRole('button').click();   // opens a dialog
  await page.getByRole('button', { name: 'OK' }).click();
  const dialog = page.getByRole('dialog');
  await page.getByText('Logout').click();
  // role-expert: verify study is not draft anymore
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.waitForResponse(
    (response) => response.url().includes('/studies/studies/light') && response.status() === 200,
    { timeout: 10000 }
  );
  await page.getByRole('cell', { name: 'My Studies Studies you' }).click();
  
  // role-expert: add member with can_admin rights
  await page.getByRole('link', { name: 'playwright-test-study-draft-create-' + randomString }).click();
  await page.getByRole('button', { name: 'Edit' }).click();
  await page.getByRole('listitem').filter({ hasText: 'Members' }).click();
  await page.getByRole('textbox', { name: 'email address / username' }).click();
  await page.getByRole('textbox', { name: 'email address / username' }).fill(process.env.ADMIN_NAME);
  await page.getByRole('button', { name: 'Invite & Add +' }).click();
  await page.locator('.cell.cell-boolean > .cell-container').first().click();
  await page.locator('.cell.cell-date > .cell-container').first().click();
  await page.locator('li:nth-child(10) > .right-col > .ng-untouched').click();
  await page.getByRole('button', { name: 'Update' }).click();
  await page.locator('.cell.cell-date > .cell-container').first().click();
  await expect(page.getByRole('heading')).toContainText('Details on study');
  await expect(page.locator('studyuser-list')).toContainText(process.env.ADMIN_NAME);
  
  // role-expert: test add DUA and protocol file 
  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('link', { name: 'playwright-test-study-draft-create-' + randomString }).click();
  await page.getByRole('button', { name: 'Edit' }).click();
  await page.getByText('Files', { exact: true }).click();
  await page.getByRole('button', { name: 'Choose file' }).click();
  await page.getByRole('group', { name: 'Data use agreement (DUA)' }).locator('input[type="file"]').setInputFiles('./tests/test_pdf.pdf');
  await page.getByRole('button', { name: 'Upload a new file' }).click();
  await page.locator('input[type="file"]').setInputFiles('./tests/test_pdf.pdf');
  await page.getByRole('button', { name: 'Update' }).click();
  
  await page.getByText('Logout').click();
});

test('expert-center-create-delete', async ({ page }) => {
  await page.goto(process.env.BASE_URL);

  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();

  await page.getByText('Medical configuration').click();
  await page.getByText('Acquisition centers').click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.locator('#name').click();
  await page.locator('#name').fill('playwright-test-center-create-' + randomString);
  await page.locator('#street').click();
  await page.locator('#street').fill('street');
  await page.locator('#street').press('Tab');
  await page.locator('#postalCode').click();
  await page.locator('#postalCode').fill('11111');
  await page.locator('#postalCode').press('Tab');
  await page.locator('#city').fill('town');
  await page.locator('#city').press('Tab');
  await page.locator('#country').fill('country');
  await page.locator('#country').press('Tab');
  await page.locator('#phoneNumber').fill('+09111222333');
  await page.locator('#phoneNumber').press('Tab');
  await page.locator('#website').fill('www.website.fr');
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on acquisition center');
  await expect(page.locator('ol')).toContainText('playwright-test-center-create-');
  await expect(page.locator('ol')).toContainText('street');
  await expect(page.locator('ol')).toContainText('11111');
  await expect(page.locator('ol')).toContainText('town');
  await expect(page.locator('ol')).toContainText('country');
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();
  
  await page.getByText('Logout').click();
});


test('expert-equipment-create-delete', async ({ page }) => {
  await page.goto(process.env.BASE_URL);

  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();

  await page.getByText('Medical configuration').click();
  await page.getByText('Center equipments').click();
  await page.getByRole('button', { name: '+ New' }).click();
  const manuModelItem = page.getByRole('listitem').filter({ hasText: 'Manufacturer model' });
  await manuModelItem.getByPlaceholder('Select an option...').click();
  const manuModelOption = page.locator('.list > div').filter({ hasText: 'Achieva 3T' });
  await expect(manuModelOption).toBeVisible();
  await manuModelOption.click();
  const acqCenterItem = page.getByRole('listitem').filter({ hasText: 'Acquisition Center' });
  await acqCenterItem.getByPlaceholder('Select an option...').click();
  const acqCenterOption = page.locator('.list > div').filter({ hasText: 'CHU Rennes' });
  await expect(acqCenterOption).toBeVisible();
  await acqCenterOption.click();
  await page.locator('#serialNumber').click();
  await page.locator('#serialNumber').fill('serial-number' + randomString);
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on center equipment');
  await expect(page.locator('ol')).toContainText('Philips Medical Systems');
  await expect(page.locator('ol')).toContainText('Achieva 3T (Mr) - Philips Medical Systems');
  await expect(page.locator('ol')).toContainText('serial-number');
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();
  await page.getByText('Center equipments').click();

  await page.getByText('Logout').click();
});

test('expert-coil-create-delete', async ({ page }) => {
  await page.goto(process.env.BASE_URL);

  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();

  await page.getByText('Medical configuration').click();
  await page.getByText('Coils').click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.locator('#name').click();
  await page.locator('#name').fill('playwright-test-coil-create-' + randomString);
  await page.locator('#serialNb').click();
  await page.locator('#serialNb').fill('serial-number-' + randomString);
  const coilTypeItem = page.getByRole('listitem').filter({ hasText: 'Coil Type' });
  await coilTypeItem.getByPlaceholder('Select an option...').click();
  const coilTypeOption = page.locator('.list > div').filter({ hasText: 'BODY' });
  await expect(coilTypeOption).toBeVisible();
  await coilTypeOption.click();
  await page.locator('#nbChannel').click();
  await page.locator('#nbChannel').fill('5');
  const acqCenterItem = page.getByRole('listitem').filter({ hasText: 'Acquisition Center' });
  await acqCenterItem.getByPlaceholder('Select an option...').click();
  const acqCenterOption = page.locator('.list > div').filter({ hasText: 'CHU Rennes' });
  await expect(acqCenterOption).toBeVisible();
  await acqCenterOption.click();
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on coil');
  await expect(page.locator('ol')).toContainText('playwright-test-coil-create-' + randomString);
  await expect(page.locator('ol')).toContainText('Serial Number');
  await expect(page.locator('ol')).toContainText('Coil Type');
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();

  await page.getByText('Logout').click();
});
