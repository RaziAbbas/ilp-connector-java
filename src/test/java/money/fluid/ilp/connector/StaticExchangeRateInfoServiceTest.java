package money.fluid.ilp.connector;

public class StaticExchangeRateInfoServiceTest {

//    private SandStaticExchangeRateService staticExchangeRateService;
//
//    @Before
//    public void setup() {
//        this.staticExchangeRateService = new SandStaticExchangeRateService();
//    }
//
//    @Test
//    public void getExchangeRate_SandSand() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(
//                SAND_GRAIN_CURRENCY, SAND_GRAIN_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(BigDecimal.ONE));
//    }
//
//    @Test
//    public void getExchangeRate_SandBlue() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(SAND_GRAIN_CURRENCY, EUR_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("1.095")));
//    }
//
//    @Test
//    public void getExchangeRate_SandRed() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(SAND_GRAIN_CURRENCY, USD_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("2")));
//    }
//
//    @Test
//    public void getExchangeRate_BlueBlue() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(EUR_CURRENCY, EUR_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(BigDecimal.ONE.setScale(10)));
//    }
//
//    @Test
//    public void getExchangeRate_BlueRed() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(EUR_CURRENCY, USD_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("0.5475000000")));
//    }
//
//    @Test
//    public void getExchangeRate_RedBlue() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(USD_CURRENCY, EUR_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("1.8264840182")));
//    }
//
//    @Test
//    public void getExchangeRate_RedRed() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.getExchangeRate(USD_CURRENCY, USD_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(BigDecimal.ONE.setScale(10)));
//    }
//
//    ////////////
//    // #fromSandTo
//    ////////////
//
//    @Test
//    public void fromSand_ToSand() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.fromSandTo(SAND_GRAIN_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(BigDecimal.ONE));
//    }
//
//    @Test
//    public void fromSand_ToBlue() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.fromSandTo(EUR_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("1.095")));
//    }
//
//    @Test
//    public void fromSand_ToRed() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.fromSandTo(USD_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("2")));
//    }
//
//
//    ////////////
//    // #toSandFrom
//    ////////////
//
//    @Test
//    public void toSand_FromSand() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.toSandFrom(SAND_GRAIN_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(BigDecimal.ONE.setScale(10)));
//    }
//
//    @Test
//    public void toSand_FromBlue() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.toSandFrom(EUR_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(EUR_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("0.9132420091")));
//    }
//
//    @Test
//    public void toSand_FromRed() throws Exception {
//        final ExchangeRateInfo actual = staticExchangeRateService.toSandFrom(USD_CURRENCY);
//
//        assertThat(actual.getSourceAssetId(), is(USD_CURRENCY));
//        assertThat(actual.getSourceAmount(), is(BigDecimal.ONE));
//        assertThat(actual.getDestinationAssetId(), is(SAND_GRAIN_CURRENCY));
//        assertThat(actual.getDestinationAmount(), is(new BigDecimal("0.5").setScale(10)));
//    }

}