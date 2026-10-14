package hr.tvz.possystem.constants;

/**
 * Predstavlja različite poglede u aplikaciji.
 */
public enum ViewType {
    /**
     * Označava da je otvoreno sučelje za blagajnika.
     */
    CASHIER,
    /**
     * Označava da je otvoreno sučelje za povrate artikala.
     */
    REFUND,
    /**
     * Označava da je otvoreno sučelje za administratora.
     */
    ADMIN,
    /**
     * Označava da je otvoreno sučelje za prijavu u aplikaciju.
     */
    LOGIN,
    /**
     * Označava da je otvoreno administratorsko sučelje za upravljanje proizvodima.
     */
    ADMIN_PRODUCT,
    /**
     * Označava da je otvoreno administratorsko sučelje za upravljanje korisnicima.
     */
    ADMIN_USERS,
    /**
     * Označava da je otvoreno administratorsko sučelje za upravljanje računima.
     */
    ADMIN_SALES,
    /**
     * Označava da je otvoreno administratorsko sučelje za upravljanje povratima.
     */
    ADMIN_REFUNDS
}
