package elv.server.result;

import java.io.Serializable;

/**
 * Result values.
 */
public class Value implements Serializable {
  public final Integer population;
  public final Integer totalCases;
  public final Integer observedCases;
  public final Double expectedCases;
  public final Double incidence;
  public final Double smr;
  public final Integer smrSignificance;
  public final Integer smrCategory;
  public final Double probability;
  public final Integer probabilityCategory;
  public final Double trend;
  public final String trendSignificance;
  public final Double trendCorrelation;
  public final Integer trendCategory;
  public final Double smoothSmr;
  public final Integer smoothSmrCategory;
  public final String string;

  private Value(Integer population, Integer totalCases, Integer observedCases, Double expectedCases, Double incidence, Double smr, Integer smrSignificance, Integer smrCategory, Double probability, Integer probabilityCategory, Double smoothSmr, Integer smoothSmrCategory, Double trend, String trendSignificance, Double trendCorrelation, Integer trendCategory) {
    this.population = population;
    this.totalCases = totalCases;
    this.observedCases = observedCases;
    this.expectedCases = expectedCases;
    this.incidence = incidence;
    this.smr = smr;
    this.smrSignificance = smrSignificance;
    this.smrCategory = smrCategory;
    this.probability = probability;
    this.probabilityCategory = probabilityCategory;
    this.smoothSmr = smoothSmr;
    this.smoothSmrCategory = smoothSmrCategory;
    this.trend = trend;
    this.trendSignificance = trendSignificance;
    this.trendCorrelation = trendCorrelation;
    this.trendCategory = trendCategory;
    this.string = Result.CSV.join(population, totalCases, observedCases, expectedCases, incidence, smr, smrSignificance, smrCategory, probability, probabilityCategory, smoothSmr, smoothSmrCategory, trend, trendSignificance, trendCorrelation, trendCategory);
  }

  @Override
  public String toString() {
    return string;
  }

  public static class Builder {
    private Integer population = null;
    private Integer totalCases = null;
    private Integer observedCases = null;
    private Double expectedCases = null;
    private Double incidence = null;
    private Double smr = null;
    private Integer smrSignificance = null;
    private Integer smrCategory = null;
    private Double probability = null;
    private Integer probabilityCategory = null;
    private Double trend = null;
    private String trendSignificance = null;
    private Double trendCorrelation = null;
    private Integer trendCategory = null;
    private Double smoothSmr = null;
    private Integer smoothSmrCategory = null;

    public Builder() {
    }

    public Builder(Value value) {
      population = value.population;
      totalCases = value.totalCases;
      observedCases = value.observedCases;
      expectedCases = value.expectedCases;
      incidence = value.incidence;
      smr = value.smr;
      smrSignificance = value.smrSignificance;
      smrCategory = value.smrCategory;
      probability = value.probability;
      probabilityCategory = value.probabilityCategory;
      trend = value.trend;
      trendSignificance = value.trendSignificance;
      trendCorrelation = value.trendCorrelation;
      trendCategory = value.trendCategory;
      smoothSmr = value.smoothSmr;
      smoothSmrCategory = value.smoothSmrCategory;
    }

    public Builder setPopulation(Integer population) {
      this.population = population;
      return this;
    }

    public Builder setTotalCases(Integer totalCases) {
      this.totalCases = totalCases;
      return this;
    }

    public Builder setObservedCases(Integer observedCases) {
      this.observedCases = observedCases;
      return this;
    }

    public Builder setExpectedCases(Double expectedCases) {
      this.expectedCases = expectedCases;
      return this;
    }

    public Builder setIncidence(Double incidence) {
      this.incidence = incidence;
      return this;
    }

    public Builder setSmr(Double smr) {
      this.smr = smr;
      return this;
    }

    public Builder setSmrSignificance(Integer smrSignificance) {
      this.smrSignificance = smrSignificance;
      return this;
    }

    public Builder setSmrCategory(Integer smrCategory) {
      this.smrCategory = smrCategory;
      return this;
    }

    public Builder setProbability(Double probability) {
      this.probability = probability;
      return this;
    }

    public Builder setProbabilityCategory(Integer probabilityCategory) {
      this.probabilityCategory = probabilityCategory;
      return this;
    }

    public Builder setTrend(Double trend) {
      this.trend = trend;
      return this;
    }

    public Builder setTrendSignificance(String trendSignificance) {
      this.trendSignificance = trendSignificance;
      return this;
    }

    public Builder setTrendCorrelation(Double trendCorrelation) {
      this.trendCorrelation = trendCorrelation;
      return this;
    }

    public Builder setTrendCategory(Integer trendCategory) {
      this.trendCategory = trendCategory;
      return this;
    }

    public Builder setSmoothSmr(Double smoothSmr) {
      this.smoothSmr = smoothSmr;
      return this;
    }

    public Builder setSmoothSmrCategory(Integer smoothSmrCategory) {
      this.smoothSmrCategory = smoothSmrCategory;
      return this;
    }

    public Value build() {
      return new Value(population, totalCases, observedCases, expectedCases, incidence, smr, smrSignificance, smrCategory, probability, probabilityCategory, smoothSmr, smoothSmrCategory, trend, trendSignificance, trendCorrelation, trendCategory);
    }
  }
}
