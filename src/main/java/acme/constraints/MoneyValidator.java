
package acme.constraints;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoneyValidator {

	@Autowired
	private MoneyValidatorRepository repository;


	public Boolean moneyValidator(final String currency) {
		String acceptedCurrencies = this.repository.findAcceptedCurrencies();
		List<String> acceptedCurrencyList = Arrays.asList(acceptedCurrencies.split(",\\s*"));

		for (String acceptedCurrency : acceptedCurrencyList)
			if (acceptedCurrency.equals(currency))
				return true;
		return false;
	}

}
