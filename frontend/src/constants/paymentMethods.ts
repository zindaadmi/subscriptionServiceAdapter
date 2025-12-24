/**
 * Valid payment methods for billing
 */
export const PAYMENT_METHODS = [
  { value: 'Credit Card', label: 'Credit Card' },
  { value: 'Debit Card', label: 'Debit Card' },
  { value: 'UPI', label: 'UPI' },
] as const

export type PaymentMethod = typeof PAYMENT_METHODS[number]['value']

