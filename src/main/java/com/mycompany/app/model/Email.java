// email = {
//     :'customerID' => "CUST"+start.to_s+(250000000000+i).to_s,
//     :'from' => Faker::Internet.free_email,
//     :'type' => "email",
//     :'to' => "CustomerSupport@bullworkbank.com",
//     #:'received' => Time.now.to_s,
//     :'received' => i.to_s,
//     :'subject' => Faker::HitchhikersGuideToTheGalaxy.quote,
//     :'body' => Faker::Lorem.paragraphs(1),
//   }


/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.mycompany.app.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Captures the key elements of a stock trade, such as the ticker symbol, price,
 * number of shares, the type of the trade (buy or sell), and an id uniquely identifying
 * the trade.
 */
public class Email {

    private final static ObjectMapper JSON = new ObjectMapper();
    static {
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Represents the type of the stock trade eg buy or sell.
     */
    // public enum TradeType {
    //     BUY,
    //     SELL
    // }

    private String customerID;
    private String from;
    private String type;
    private String to;
    private String received;
    private String subject;
    private String body;

    public Email() {
    }

    public Email(String tickerSymbol, String from, String type, String to, String received, String subject, String body) {
        this.customerID = tickerSymbol;
        this.from = from;
        this.type = type;
        this.to = to;
        this.received = received;
        this.subject = subject;
    }

    public String getTickerSymbol() {
        return customerID;
    }

    public String getFrom() {
        return from;
    }

    public String getType() {
        return type;
    }

    public String getTo() {
        return to;
    }

    public String getReceived() {
        return received;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public byte[] toJsonAsBytes() {
        try {
            return JSON.writeValueAsBytes(this);
        } catch (IOException e) {
            return null;
        }
    }

    public static Email fromJsonAsBytes(byte[] bytes) {
        try {
            return JSON.readValue(bytes, Email.class);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("ID %d: %s %d shares of %s for $%.02f",
             to, type, customerID, from);
    }

}
