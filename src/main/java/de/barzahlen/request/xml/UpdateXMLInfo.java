/**
 * Barzahlen Payment Module SDK NOTICE OF LICENSE This program is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; version 3 of the
 * License This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/
 *
 * @copyright Copyright (c) 2012 Zerebro Internet GmbH
 *            (http://www.barzahlen.de/)
 * @author Jesus Javier Nuno Garcia
 * @license http://opensource.org/licenses/GPL-3.0 GNU General Public License,
 *          version 3 (GPL-3.0)
 */
package de.barzahlen.request.xml;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses the XML data retrieved from the server for the "update" request
 *
 * @author Jesus Javier Nuno Garcia
 */
public class UpdateXMLInfo extends XMLInfo {

	/**
	 * The transaction id
	 */
	protected String transactionId;

	/**
	 * Result value
	 */
	protected String result;

	/**
	 * Information hash
	 */
	protected String hash;

	/**
	 * The error message in case of bad request
	 */
	protected String errorMessage;

	/**
	 * Default constructor
	 */
	public UpdateXMLInfo() {
		super();
		this.paramsAmountExpected = 3;
		this.transactionId = "";
		this.result = "0";
		this.hash = "";
		this.errorMessage = "";
	}

	@Override
	protected void initHandlers() {
		this.normalHandler = new DefaultHandler() {

			String currentStartingTag = "";

			String currentEndingTag = "";

			String currentLine = "";

			boolean setCurrentLine = false;

			boolean tid = false;

			boolean res = false;

			boolean hsh = false;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

				if (qName.equalsIgnoreCase("transaction-id")) {
					this.currentStartingTag = "<" + qName + ">";
					this.tid = true;
					this.setCurrentLine = true;
				}

				if (qName.equalsIgnoreCase("result")) {
					this.currentStartingTag = "<" + qName + ">";
					this.res = true;
					this.setCurrentLine = true;
				}

				if (qName.equalsIgnoreCase("hash")) {
					this.currentStartingTag = "<" + qName + ">";
					this.hsh = true;
					this.setCurrentLine = true;
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				try {
					this.currentEndingTag = "<" + qName + ">";

					if (this.currentStartingTag.equals(this.currentEndingTag)) {
						if (this.tid) {
							int currentEndingTagPosition = this.currentLine.indexOf("</" + qName + ">");
							UpdateXMLInfo.this.transactionId = StringEscapeUtils.unescapeHtml(this.currentLine.substring(0,
									currentEndingTagPosition));
							this.tid = false;
							UpdateXMLInfo.this.paramsAmountReceived++;
						}

						if (this.res) {
							int currentEndingTagPosition = this.currentLine.indexOf("</" + qName + ">");
							UpdateXMLInfo.this.result = StringEscapeUtils.unescapeHtml(this.currentLine.substring(0,
									currentEndingTagPosition));
							this.res = false;
							UpdateXMLInfo.this.paramsAmountReceived++;
						}

						if (this.hsh) {
							int currentEndingTagPosition = this.currentLine.indexOf("</" + qName + ">");
							UpdateXMLInfo.this.hash = StringEscapeUtils.unescapeHtml(this.currentLine
									.substring(0, currentEndingTagPosition));
							this.hsh = false;
							UpdateXMLInfo.this.paramsAmountReceived++;
						}
					}
				} catch (Exception e) {
					UpdateXMLInfo.this.paramsAmountReceived = -1;
				}
			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (this.setCurrentLine) {
					if (this.tid) {
						this.currentLine = new String(ch);
						this.currentLine = this.currentLine.substring(this.currentLine.indexOf(this.currentStartingTag)
								+ this.currentStartingTag.length());
						this.setCurrentLine = false;
					}

					if (this.res) {
						this.currentLine = new String(ch);
						this.currentLine = this.currentLine.substring(this.currentLine.indexOf(this.currentStartingTag)
								+ this.currentStartingTag.length());
						this.setCurrentLine = false;
					}

					if (this.hsh) {
						this.currentLine = new String(ch);
						this.currentLine = this.currentLine.substring(this.currentLine.indexOf(this.currentStartingTag)
								+ this.currentStartingTag.length());
						this.setCurrentLine = false;
					}
				}
			}
		};

		this.errorHandler = new DefaultHandler() {

			boolean res = false;

			boolean err = false;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

				if (qName.equalsIgnoreCase("result")) {
					this.res = true;
				}

				if (qName.equalsIgnoreCase("error-message")) {
					this.err = true;
				}
			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (this.res) {
					UpdateXMLInfo.this.result = new String(ch, start, length);
					this.res = false;
				}

				if (this.err) {
					UpdateXMLInfo.this.errorMessage = new String(ch, start, length);
					this.err = false;
				}
			}
		};
	}

	/**
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return this.transactionId;
	}

	/**
	 * @return the result
	 */
	public int getResult() {
		return Integer.valueOf(this.result);
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * @return the error
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * @return the paramsAmountReceived
	 */
	public int getParamsAmountReceived() {
		return this.paramsAmountReceived;
	}
}
