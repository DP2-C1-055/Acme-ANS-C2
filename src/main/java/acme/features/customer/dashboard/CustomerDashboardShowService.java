
package acme.features.customer.dashboard;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.constraints.MoneyValidatorRepository;
import acme.entities.booking.Booking;
import acme.entities.booking.TravelClass;
import acme.entities.passenger.Passenger;
import acme.forms.CustomerDashboard;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerDashboardShowService extends AbstractGuiService<Customer, CustomerDashboard> {

	@Autowired
	CustomerDashboardRepository			repository;

	@Autowired
	protected MoneyValidatorRepository	moneyValidator;


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Customer customer;
		CustomerDashboard dashboard;

		int customerId;
		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		customer = this.repository.getCustomerById(customerId);
		String money = this.moneyValidator.findAcceptedCurrencies();
		String[] currenciesArray = money.split(",\\s*");
		List<String> currenciesList = new ArrayList<>(Arrays.asList(currenciesArray));

		Integer countOfPassengersInBookings;
		Integer minimumPassengersInBookings;
		Integer maximumPassengersInBookings;
		Double averagePassengersInBookings;
		Double standardDeviationPassengersInBookings;

		List<String> lastFiveDestinations;
		Map<String, Double> moneySpentInBookingsLastYear;
		Map<String, Integer> numberOfBookingsByTravelClass;

		Map<String, Integer> countOfBookingsLastFiveYears;
		Map<String, Double> averageCostOfBookingsLastFiveYears;
		Map<String, Double> minimumCostOfBookingsLastFiveYears;
		Map<String, Double> maximumCostOfBookingsLastFiveYears;
		Map<String, Double> standardDeviationCostOfBookingsLastFiveYears;

		lastFiveDestinations = this.getLastFiveDestionation(customerId);
		moneySpentInBookingsLastYear = this.calculateCountOfBookingsLastYears(customer, currenciesList);
		numberOfBookingsByTravelClass = this.calculateNumberOfBookingsByTravelClass(customer);

		countOfPassengersInBookings = this.countPassengersInBookings(customerId);
		minimumPassengersInBookings = this.getMinimumPassengersInBookings(customerId);
		maximumPassengersInBookings = this.maximumPassengersInBookings(customerId);
		averagePassengersInBookings = this.averagePassengersInBookings(customerId);
		standardDeviationPassengersInBookings = this.standardDeviationPassengersInBookings(customerId);

		countOfBookingsLastFiveYears = this.calculateCountOfBookingsLastFiveYears(customer, currenciesList);
		averageCostOfBookingsLastFiveYears = this.calculateAveragefBookingsLastFiveYears(customer, currenciesList);
		minimumCostOfBookingsLastFiveYears = this.calculateMinimumCostOfBookingsLastFiveYears(customer, currenciesList);
		maximumCostOfBookingsLastFiveYears = this.calculateMaximumCostOfBookingsLastFiveYears(customer, currenciesList);
		standardDeviationCostOfBookingsLastFiveYears = this.calculateStandardDeviationOfBookingsLastFiveYears(customer, currenciesList);

		dashboard = new CustomerDashboard();

		dashboard.setLastFiveDestinations(lastFiveDestinations);
		dashboard.setMoneySpentInBookingsLastYear(moneySpentInBookingsLastYear);
		dashboard.setNumberOfBookingsByTravelClass(numberOfBookingsByTravelClass);

		dashboard.setCountOfPassengersInBookings(countOfPassengersInBookings);
		dashboard.setMinimumPassengersInBookings(minimumPassengersInBookings);
		dashboard.setMaximumPassengersInBookings(maximumPassengersInBookings);
		dashboard.setAveragePassengersInBookings(averagePassengersInBookings);
		dashboard.setStandardDeviationPassengersInBookings(standardDeviationPassengersInBookings);

		dashboard.setCountOfBookingsLastFiveYears(countOfBookingsLastFiveYears);
		dashboard.setAverageCostOfBookingsLastFiveYears(averageCostOfBookingsLastFiveYears);
		dashboard.setMinimumCostOfBookingsLastFiveYears(minimumCostOfBookingsLastFiveYears);
		dashboard.setMaximumCostOfBookingsLastFiveYears(maximumCostOfBookingsLastFiveYears);
		dashboard.setStandardDeviationCostOfBookingsLastFiveYears(standardDeviationCostOfBookingsLastFiveYears);

		super.getBuffer().addData(dashboard);
	}

	private Date getFiveYearsAgo() {
		Date today = MomentHelper.getCurrentMoment();
		return MomentHelper.deltaFromMoment(today, -5, ChronoUnit.YEARS);
	}

	private Date getOneYearAgo() {
		Date today = MomentHelper.getCurrentMoment();
		return MomentHelper.deltaFromMoment(today, -1, ChronoUnit.YEARS);

	}

	private Map<String, Integer> calculateNumberOfBookingsByTravelClass(final Customer customer) {
		Map<String, Integer> bookingPerTravelClassMap = new HashMap<>();
		List<Object[]> bookingPerTravelClass = this.repository.findBookingCountByTravelClass(customer);
		for (Object[] entry : bookingPerTravelClass) {
			String travelClass = entry[0].toString(); // Convertir travelClass a String
			Integer count = ((Long) entry[1]).intValue();
			bookingPerTravelClassMap.put(travelClass, count);
		}

		return bookingPerTravelClassMap;
	}

	private Map<String, Double> calculateCountOfBookingsLastYears(final Customer customer, final List<String> currenciesList) {
		Map<String, Double> averagePerCurrency = new HashMap<>();
		Date oneYearAgo = this.getOneYearAgo();
		Double cost = 0.0;
		for (String currency : currenciesList) {
			Collection<Booking> bookingPerCurrency = this.repository.findBookingsOfLastFiveYears(customer, currency, oneYearAgo);
			if (!bookingPerCurrency.isEmpty()) {
				for (Booking b : bookingPerCurrency)
					cost += b.getBookingPrice().getAmount();
				averagePerCurrency.put(currency, cost);
				cost = 0.0;
			} else
				averagePerCurrency.put(currency, null);

		}
		return averagePerCurrency;

	}

	private List<String> getLastFiveDestionation(final int customerId) {
		List<String> lastDestionationFlight = new ArrayList<>();
		Collection<Booking> lastDestination = this.repository.findLastBookingConfirmedByCustomer(customerId);
		int i = 0;
		if (!lastDestination.isEmpty())
			for (Booking b : lastDestination) {
				i += 1;
				if (i <= 5)
					lastDestionationFlight.add(b.getFlight().getDestinationCity());

			}
		else
			lastDestionationFlight = null;

		return lastDestionationFlight;
	}

	private Map<String, Double> calculateStandardDeviationOfBookingsLastFiveYears(final Customer customer, final List<String> currenciesList) {
		Map<String, Double> standardDeviationPerCurrency = new HashMap<>();
		Date fiveYearsAgo = this.getFiveYearsAgo();

		for (String currency : currenciesList) {
			Collection<Booking> bookingPerCurrency = this.repository.findBookingsOfLastFiveYears(customer, currency, fiveYearsAgo);

			if (!bookingPerCurrency.isEmpty()) {
				double sum = 0.0;

				List<Double> sumOfSquare = new ArrayList<>();
				for (Booking b : bookingPerCurrency) {
					double amount = b.getBookingPrice().getAmount();
					sumOfSquare.add(amount * amount);
					sum += amount;
				}

				double totalSquare = 0.0;
				for (Double d : sumOfSquare)
					totalSquare += d;

				double average = sum / bookingPerCurrency.size();

				double variance = totalSquare / average;
				double standardDeviation = Math.sqrt(variance);

				standardDeviationPerCurrency.put(currency, standardDeviation);
			} else
				standardDeviationPerCurrency.put(currency, null);
		}
		return standardDeviationPerCurrency;
	}

	private Map<String, Double> calculateMaximumCostOfBookingsLastFiveYears(final Customer customer, final List<String> currenciesList) {
		Map<String, Double> averagePerCurrency = new HashMap<>();
		Date fiveYearsAgo = this.getFiveYearsAgo();
		Double cost = Double.MIN_VALUE;
		Double bookingCost = 0.0;
		for (String currency : currenciesList) {
			Collection<Booking> bookingPerCurrency = this.repository.findBookingsOfLastFiveYears(customer, currency, fiveYearsAgo);
			if (!bookingPerCurrency.isEmpty()) {
				for (Booking b : bookingPerCurrency) {
					bookingCost = b.getBookingPrice().getAmount();
					cost = bookingCost > cost ? bookingCost : cost;
				}
				averagePerCurrency.put(currency, cost);
				cost = 0.0;
				bookingCost = 0.0;
			} else
				averagePerCurrency.put(currency, null);

		}
		return averagePerCurrency;

	}

	private Map<String, Double> calculateMinimumCostOfBookingsLastFiveYears(final Customer customer, final List<String> currenciesList) {
		Map<String, Double> averagePerCurrency = new HashMap<>();
		Date fiveYearsAgo = this.getFiveYearsAgo();
		Double cost = Double.MAX_VALUE;
		Double bookingCost = 0.0;
		for (String currency : currenciesList) {
			Collection<Booking> bookingPerCurrency = this.repository.findBookingsOfLastFiveYears(customer, currency, fiveYearsAgo);
			if (!bookingPerCurrency.isEmpty()) {
				for (Booking b : bookingPerCurrency) {
					bookingCost = b.getBookingPrice().getAmount();
					cost = bookingCost < cost ? bookingCost : cost;
				}
				averagePerCurrency.put(currency, cost);
				cost = 0.0;
				bookingCost = 0.0;
			} else
				averagePerCurrency.put(currency, null);

		}
		return averagePerCurrency;

	}

	private Map<String, Double> calculateAveragefBookingsLastFiveYears(final Customer customer, final List<String> currenciesList) {
		Map<String, Double> averagePerCurrency = new HashMap<>();
		Date fiveYearsAgo = this.getFiveYearsAgo();
		double cost = 0.0;
		int i = 0;
		for (String currency : currenciesList) {
			Collection<Booking> bookingPerCurrency = this.repository.findBookingsOfLastFiveYears(customer, currency, fiveYearsAgo);
			if (!bookingPerCurrency.isEmpty()) {
				for (Booking b : bookingPerCurrency) {
					i += 1;
					cost += b.getBookingPrice().getAmount();
				}
				double averageCost = cost / i;
				averagePerCurrency.put(currency, averageCost);
				cost = 0.0;
				i = 0;
			} else
				averagePerCurrency.put(currency, null);

		}
		return averagePerCurrency;
	}

	private Map<String, Integer> calculateCountOfBookingsLastFiveYears(final Customer customer, final List<String> currenciesList) {
		Map<String, Integer> averagePerCurrency = new HashMap<>();
		Date fiveYearsAgo = this.getFiveYearsAgo();
		for (String currency : currenciesList) {
			Collection<Booking> bookingPerCurrency = this.repository.findBookingsOfLastFiveYears(customer, currency, fiveYearsAgo);
			if (!bookingPerCurrency.isEmpty())
				averagePerCurrency.put(currency, bookingPerCurrency.size());
			else
				averagePerCurrency.put(currency, null);

		}
		return averagePerCurrency;

	}

	private Integer countPassengersInBookings(final int customerId) {
		int totalPassengers = 0;
		Collection<Booking> bookingConfirmed = this.repository.findBookingConfirmedByCustomer(customerId);
		for (Booking b : bookingConfirmed) {
			Collection<Passenger> passengers = this.repository.findPassengenrsByBooking(b.getId());
			totalPassengers += passengers.size();
		}
		return totalPassengers;
	}

	private Integer getMinimumPassengersInBookings(final int customerId) {
		Integer minCount;
		Collection<Long> bookingConfirmed = this.repository.findBookingIdConfirmedByCustomer(customerId);
		Collection<Integer> bookingConfirmedIntegers = bookingConfirmed.stream().map(Long::intValue) // Convierte cada Long a Integer
			.collect(Collectors.toList()); // Recoge el resultado en una lista
		List<Object[]> bookingRecordMap = this.repository.countBookingsById(bookingConfirmedIntegers);
		Map<Integer, Integer> bookingRecordGroup = this.getBookingCounts(bookingRecordMap);
		if (bookingRecordGroup.size() != 0)
			minCount = Collections.min(bookingRecordGroup.values());
		else
			minCount = null;
		return minCount;

	}

	private Integer maximumPassengersInBookings(final int customerId) {
		Integer maxCount;
		Collection<Long> bookingConfirmed = this.repository.findBookingIdConfirmedByCustomer(customerId);
		Collection<Integer> bookingConfirmedIntegers = bookingConfirmed.stream().map(Long::intValue) // Convierte cada Long a Integer
			.collect(Collectors.toList()); // Recoge el resultado en una lista
		List<Object[]> bookingRecordMap = this.repository.countBookingsById(bookingConfirmedIntegers);
		Map<Integer, Integer> bookingRecordGroup = this.getBookingCounts(bookingRecordMap);
		if (bookingRecordGroup.size() != 0)
			maxCount = Collections.max(bookingRecordGroup.values());
		else
			maxCount = null;
		return maxCount;

	}

	private Double averagePassengersInBookings(final int customerId) {
		Double averageCount;
		Collection<Long> bookingConfirmed = this.repository.findBookingIdConfirmedByCustomer(customerId);
		Collection<Integer> bookingConfirmedIntegers = bookingConfirmed.stream().map(Long::intValue) // Convierte cada Long a Integer
			.collect(Collectors.toList()); // Recoge el resultado en una lista
		List<Object[]> bookingRecordMap = this.repository.countBookingsById(bookingConfirmedIntegers);
		Map<Integer, Integer> bookingRecordGroup = this.getBookingCounts(bookingRecordMap);
		if (bookingRecordGroup.size() != 0) {
			int totalPassengers = bookingRecordGroup.values().stream().mapToInt(Integer::intValue) // Convertir cada Integer a int
				.sum();
			averageCount = (double) totalPassengers / bookingRecordGroup.size();
		} else
			averageCount = null;
		return averageCount;

	}

	private Double standardDeviationPassengersInBookings(final int customerId) {
		Double standardDeviation = 0.0;
		Collection<Long> bookingConfirmed = this.repository.findBookingIdConfirmedByCustomer(customerId);
		Collection<Integer> bookingConfirmedIntegers = bookingConfirmed.stream().map(Long::intValue) // Convierte cada Long a Integer
			.collect(Collectors.toList()); // Recoge el resultado en una lista
		List<Object[]> bookingRecordMap = this.repository.countBookingsById(bookingConfirmedIntegers);
		Map<Integer, Integer> bookingRecordGroup = this.getBookingCounts(bookingRecordMap);
		if (bookingRecordGroup.size() != 0) {
			double averagePassanger = this.averagePassengersInBookings(customerId);

			double sumOfSquares = bookingRecordGroup.values().stream().mapToDouble(count -> Math.pow(count - averagePassanger, 2)).sum();
			double variance = sumOfSquares / bookingRecordGroup.size();
			standardDeviation = Math.sqrt(variance);
		} else
			standardDeviation = null;

		return standardDeviation;

	}

	public Map<Integer, Integer> getBookingCounts(final List<Object[]> bookingRecordMap) {
		return bookingRecordMap.stream().collect(Collectors.toMap(row -> (int) row[0], // booking.id
			row -> ((Long) row[1]).intValue()  // Convertimos Long a Integer
		));
	}

	@Override
	public void unbind(final CustomerDashboard object) {
		assert object != null;
		Dataset dataset;
		String nullValues;
		Locale local;

		List<String> travelClass = new ArrayList<>();
		travelClass.add(TravelClass.BUSINESS.toString());
		travelClass.add(TravelClass.ECONOMY.toString());

		local = super.getRequest().getLocale();
		nullValues = local.equals(Locale.ENGLISH) ? "No Data" : "Sin Datos";

		String money = this.moneyValidator.findAcceptedCurrencies();
		String[] currenciesArray = money.split(",\\s*");

		dataset = super.unbindObject(object, "countOfPassengersInBookings", "minimumPassengersInBookings", "maximumPassengersInBookings", "averagePassengersInBookings", "standardDeviationPassengersInBookings", "countOfBookingsLastFiveYears",
			"averageCostOfBookingsLastFiveYears", "minimumCostOfBookingsLastFiveYears", "maximumCostOfBookingsLastFiveYears", "standardDeviationCostOfBookingsLastFiveYears", "lastFiveDestinations", "moneySpentInBookingsLastYear",
			"numberOfBookingsByTravelClass");
		dataset.put("nullValues", nullValues);
		super.getResponse().addData(dataset);
		super.getResponse().addGlobal("currency", currenciesArray);
		super.getResponse().addGlobal("travelClass", travelClass);

	}
}
