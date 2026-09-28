package works.weave.socks.orders.resources;

import works.weave.socks.orders.entities.Address;
import works.weave.socks.orders.entities.Card;
import works.weave.socks.orders.entities.Customer;

public class PaymentRequest {
    private Address address;
    private Card card;
    private Customer customer;
    private float amount;
    private String currency;

    // For jackson
    public PaymentRequest() {
    }

    public PaymentRequest(Address address, Card card, Customer customer, float amount, String currency) {
        this.address = address;
        this.customer = customer;
        this.card = card;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "address=" + address +
                ", card=" + card +
                ", customer=" + customer +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        // A simple placeholder for currency validation
        if (!validateCurrency(currency)) {
            throw new IllegalArgumentException("Unsupported currency code: " + currency);
        }
        this.currency = currency;
    }

    // Sample method for validating currency
    private boolean validateCurrency(String currency) {
        String[] supportedCurrencies = {"USD", "EUR", "GBP"}; // A sample set of supported codes
        for (String supportedCurrency : supportedCurrencies) {
            if (supportedCurrency.equals(currency)) {
                return true;
            }
        }
        return false;
    }
}
