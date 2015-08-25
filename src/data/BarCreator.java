package data;

import constants.Constants.BAR_SIZE;

public class BarCreator {

	public static void main(String[] args) {
		Converter.processTickDataIntoBars(TickConstants.TICK_NAME_OKCOIN_BTC_CNY, BAR_SIZE.BAR_1M);
	}

}
