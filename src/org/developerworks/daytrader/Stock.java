package org.developerworks.daytrader;

import org.developerworks.stocks.Stocks;

public class Stock implements Cloneable{
	public Stock(String symbol, String name, double price) {
		this.symbol = symbol;
		this.name = name;
		this.price = price;
	}
	
	public Stock(){
		
	}
	
	private String symbol;
	private String name;
	private double price;
	
	public String getSymbol() {
		return symbol;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	@Override
	public Object clone(){
		return new Stock(symbol, name, price);
	}
	

	@Override 
	public int hashCode(){
		return symbol.hashCode();
	}
	
	@Override 
	public boolean equals(Object other){
		if (other instanceof Stock){
			Stock otherStock = (Stock) other;
			return otherStock.symbol.equals(symbol);
		}
		return false;
	}
	
	@Override
	public String toString(){
		return name + "(" + symbol + "): $" + price;
	}
	
	public static Stock fromQuote(Stocks.Quote quote){
		return new Stock(quote.getSymbol(), quote.getName(), quote.getPrice());
	}
	
	public Stock[] fromPortfolio(Stocks.Portfolio portfolio){
		Stock[] stocks = new Stock[portfolio.getQuoteCount()];
		for(int i=0;i<stocks.length;i++){
			stocks[i] = fromQuote(portfolio.getQuote(i));
		}
		return stocks;
	}
}
