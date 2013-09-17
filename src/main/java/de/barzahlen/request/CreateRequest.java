/**
 * Barzahlen Payment Module SDK
 *
 * NOTICE OF LICENSE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 *
 * @copyright Copyright (c) 2012 Zerebro Internet GmbH (http://www.barzahlen.de/)
 * @author Jesus Javier Nuno Garcia
 * @license http://opensource.org/licenses/GPL-3.0  GNU General Public License, version 3 (GPL-3.0)
 */
package de.barzahlen.request;

import de.barzahlen.Barzahlen;
import de.barzahlen.BarzahlenApiRequest;
import de.barzahlen.configuration.Configuration;
import de.barzahlen.enums.RequestErrorCode;
import de.barzahlen.response.CreateResponse;
import de.barzahlen.response.ErrorResponse;
import de.barzahlen.tools.HashTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements the create transaction web service
 *
 * @author Jesus Javier Nuno Garcia
 */
public final class CreateRequest extends ServerRequest {

	/**
	 * Log file for the logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateRequest.class);

	/**
	 * The xml info retrieved from the server response.
	 */
	private CreateResponse createResponse;

	/**
	 * Request object
	 */
	private BarzahlenApiRequest request;

	/**
	 * Indicates whether the request was successful
	 */
	private boolean successful = false;

	/**
	 * Constructor with parameters for the "create" request
	 */
	public CreateRequest(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Executes the create request.
	 *
	 * @param parameters The parameters for the request. There are necessary "order_id"
	 *                   and "currency" (ISO 4217).
	 * @throws Exception
	 */
	public CreateResponse create(Map<String, String> parameters) throws Exception {
		executeServerRequest(Barzahlen.BARZAHLEN_CREATE_URL, assembleParameters(parameters));

		return createResponse;
	}

	@Override
	protected List<String> getParametersTemplate() {
		List<String> parametersTemplate = new ArrayList<String>(14);

		parametersTemplate.add("shop_id");
		parametersTemplate.add("customer_email");
		parametersTemplate.add("amount");
		parametersTemplate.add("currency");
		parametersTemplate.add("language");
		parametersTemplate.add("order_id");
		parametersTemplate.add("customer_street_nr");
		parametersTemplate.add("customer_zipcode");
		parametersTemplate.add("customer_city");
		parametersTemplate.add("customer_country");
		parametersTemplate.add("custom_var_0");
		parametersTemplate.add("custom_var_1");
		parametersTemplate.add("custom_var_2");
		parametersTemplate.add("payment_key");

		return parametersTemplate;
	}

	@Override
	protected boolean executeServerRequest(String targetUrl, String urlParameters) throws Exception {
		request = new BarzahlenApiRequest(targetUrl, CreateResponse.class);
		successful = request.doRequest(urlParameters);

		if (isSandboxMode()) {
			logger.debug("Response code: " + request.getResponseCode() + ". Response message: " + request.getResponseMessage()
					+ ". Parameters sent: " + ServerRequest.formatReadableParameters(urlParameters));

			logger.debug(request.getResult());

			if (isSuccessful()) {
				createResponse = (CreateResponse) request.getResponse();

				logger.debug(createResponse.getTransactionId());
				logger.debug(createResponse.getPaymentSlipLink());
				logger.debug(createResponse.getExpirationNotice());
				logger.debug(createResponse.getInfotext1());
				logger.debug(createResponse.getInfotext2());
				logger.debug(String.valueOf(createResponse.getResult()));
				logger.debug(createResponse.getHash());
			}
		}

		if (!isSuccessful()) {
			return errorAction(targetUrl, urlParameters, RequestErrorCode.XML_ERROR, "Payment slip request failed - retry", "Error received from the server. Response code: " + request.getResponseCode() + ". Response message: " + request.getResponseMessage());
		} else if (!compareHashes()) {
			return errorAction(targetUrl, urlParameters, RequestErrorCode.HASH_ERROR, "Payment slip request failed - retry", "Data received is not correct (hashes do not match)");
		} else {
			BARZAHLEN_REQUEST_ERROR_CODE = RequestErrorCode.SUCCESS;

			createResponse = (CreateResponse) request.getResponse();

			return true;
		}
	}

	@Override
	protected boolean compareHashes() {
		String data = createResponse.getTransactionId() + ";" + createResponse.getPaymentSlipLink() + ";"
				+ createResponse.getExpirationNotice() + ";" + createResponse.getInfotext1() + ";"
				+ createResponse.getInfotext2() + ";" + createResponse.getResult() + ";" + getPaymentKey();
		String hash = HashTools.getHash(data);

		if (isSandboxMode()) {
			logger.debug("Calculated hash: " + hash);
			logger.debug("Received hash  : " + createResponse.getHash());
		}

		return hash.equals(createResponse.getHash());
	}

	public CreateResponse getCreateResponse() {
		return createResponse;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public ErrorResponse getErrorResponse() {
		return (ErrorResponse) request.getResponse();
	}
}
