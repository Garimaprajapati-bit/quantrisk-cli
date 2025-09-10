// QuantRiskCLI.java - Main CLI Application
package com.quantrisk;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class QuantRiskCLI {
    private static final Scanner scanner = new Scanner(System.in);
    private static final RiskCalculator riskCalc = new RiskCalculator();
    private static final MarketDataFetcher dataFetcher = new MarketDataFetcher();
    
    public static void main(String[] args) {
        printHeader();
        
        if (args.length > 0) {
            // Batch mode for automation
            runBatchMode(args);
        } else {
            // Interactive mode
            runInteractiveMode();
        }
    }
    
    private static void printHeader() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║            QuantRisk CLI v1.0            ║");
        System.out.println("║      Portfolio Risk Calculator Tool      ║");
        System.out.println("║     For Quantitative Developers         ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }
    
    private static void runInteractiveMode() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1" -> calculatePortfolioVaR();
                    case "2" -> runStressTest();
                    case "3" -> correlationAnalysis();
                    case "4" -> loadPortfolioFromCSV();
                    case "5" -> monteCarloSimulation();
                    case "6" -> exportResults();
                    case "7" -> batchProcessing();
                    case "q" -> {
                        System.out.println("Exiting QuantRisk CLI. Happy trading!");
                        return;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    private static void printMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("QUANTITATIVE RISK ANALYSIS MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. 📊 Calculate Portfolio VaR");
        System.out.println("2. 🔥 Stress Testing");
        System.out.println("3. 🔗 Correlation Analysis");
        System.out.println("4. 📂 Load Portfolio from CSV");
        System.out.println("5. 🎲 Monte Carlo Simulation");
        System.out.println("6. 💾 Export Results");
        System.out.println("7. ⚡ Batch Processing");
        System.out.println("q. 🚪 Quit");
        System.out.println("=".repeat(50));
        System.out.print("Select option: ");
    }
    
    private static void calculatePortfolioVaR() {
        System.out.println("\n📊 PORTFOLIO VaR CALCULATION");
        System.out.println("-".repeat(40));
        
        System.out.print("Enter symbols (comma-separated, e.g., AAPL,MSFT,GOOGL): ");
        String symbolsInput = scanner.nextLine().trim();
        String[] symbols = symbolsInput.split(",");
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = symbols[i].trim().toUpperCase();
        }
        
        System.out.print("Enter weights (comma-separated, e.g., 0.4,0.3,0.3): ");
        String weightsInput = scanner.nextLine().trim();
        String[] weightStrs = weightsInput.split(",");
        double[] weights = new double[weightStrs.length];
        for (int i = 0; i < weightStrs.length; i++) {
            weights[i] = Double.parseDouble(weightStrs[i].trim());
        }
        
        System.out.print("Portfolio value (USD): ");
        double portfolioValue = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.print("Confidence level (e.g., 0.95): ");
        double confidence = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.print("Time horizon (days): ");
        int days = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.println("\n🔄 Fetching market data and calculating VaR...");
        
        // Multi-threaded data fetching
        CompletableFuture<Map<String, double[]>> dataFuture = 
            dataFetcher.fetchReturnsAsync(Arrays.asList(symbols));
        
        try {
            Map<String, double[]> returns = dataFuture.get(30, TimeUnit.SECONDS);
            VaRResult result = riskCalc.calculatePortfolioVaR(returns, weights, 
                                                            portfolioValue, confidence, days);
            
            printVaRResults(result);
            
        } catch (Exception e) {
            System.err.println("Error calculating VaR: " + e.getMessage());
        }
    }
    
    private static void runStressTest() {
        System.out.println("\n🔥 STRESS TESTING");
        System.out.println("-".repeat(40));
        
        System.out.println("Select stress scenario:");
        System.out.println("1. 2008 Financial Crisis");
        System.out.println("2. COVID-19 Crash (March 2020)");
        System.out.println("3. Custom Shock");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        
        System.out.print("Enter symbols (comma-separated): ");
        String symbolsInput = scanner.nextLine().trim();
        String[] symbols = symbolsInput.split(",");
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = symbols[i].trim().toUpperCase();
        }
        
        System.out.print("Portfolio value (USD): ");
        double portfolioValue = Double.parseDouble(scanner.nextLine().trim());
        
        StressTestResult result;
        switch (choice) {
            case "1" -> result = riskCalc.stress2008Crisis(symbols, portfolioValue);
            case "2" -> result = riskCalc.stressCovid2020(symbols, portfolioValue);
            case "3" -> {
                System.out.print("Enter shock percentage (e.g., -0.30 for -30%): ");
                double shock = Double.parseDouble(scanner.nextLine().trim());
                result = riskCalc.customStressTest(symbols, portfolioValue, shock);
            }
            default -> {
                System.out.println("Invalid choice");
                return;
            }
        }
        
        printStressTestResults(result);
    }
    
    private static void correlationAnalysis() {
        System.out.println("\n🔗 CORRELATION ANALYSIS");
        System.out.println("-".repeat(40));
        
        System.out.print("Enter symbols for correlation matrix (comma-separated): ");
        String symbolsInput = scanner.nextLine().trim();
        String[] symbols = symbolsInput.split(",");
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = symbols[i].trim().toUpperCase();
        }
        
        System.out.println("\n🔄 Calculating correlations...");
        
        try {
            CompletableFuture<Map<String, double[]>> dataFuture = 
                dataFetcher.fetchReturnsAsync(Arrays.asList(symbols));
            Map<String, double[]> returns = dataFuture.get(30, TimeUnit.SECONDS);
            
            double[][] correlationMatrix = riskCalc.calculateCorrelationMatrix(returns, symbols);
            printCorrelationMatrix(symbols, correlationMatrix);
            
        } catch (Exception e) {
            System.err.println("Error calculating correlations: " + e.getMessage());
        }
    }
    
    private static void loadPortfolioFromCSV() {
        System.out.println("\n📂 LOAD PORTFOLIO FROM CSV");
        System.out.println("-".repeat(40));
        
        System.out.print("Enter CSV file path: ");
        String filePath = scanner.nextLine().trim();
        
        try {
            Portfolio portfolio = loadPortfolioFromFile(filePath);
            System.out.println("✅ Portfolio loaded successfully!");
            System.out.println("Symbols: " + portfolio.getSymbols().size());
            System.out.println("Total value: $" + String.format("%,.2f", portfolio.getTotalValue()));
            
            // Auto-calculate VaR for loaded portfolio
            System.out.print("\nCalculate VaR for this portfolio? (y/n): ");
            if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                calculateVaRForPortfolio(portfolio);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading portfolio: " + e.getMessage());
        }
    }
    
    private static void monteCarloSimulation() {
        System.out.println("\n🎲 MONTE CARLO VaR SIMULATION");
        System.out.println("-".repeat(40));
        
        System.out.print("Enter symbols (comma-separated): ");
        String symbolsInput = scanner.nextLine().trim();
        String[] symbols = symbolsInput.split(",");
        
        System.out.print("Number of simulations (e.g., 10000): ");
        int numSims = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Portfolio value (USD): ");
        double portfolioValue = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.println("\n🔄 Running " + numSims + " Monte Carlo simulations...");
        
        try {
            MonteCarloResult result = riskCalc.runMonteCarlo(symbols, portfolioValue, numSims);
            printMonteCarloResults(result);
            
        } catch (Exception e) {
            System.err.println("Error running simulation: " + e.getMessage());
        }
    }
    
    private static void runBatchMode(String[] args) {
        System.out.println("🤖 BATCH MODE");
        
        if (args[0].equals("--portfolio") && args.length >= 2) {
            try {
                Portfolio portfolio = loadPortfolioFromFile(args[1]);
                VaRResult result = calculateVaRForPortfolio(portfolio);
                
                // Output in JSON for automation
                ObjectMapper mapper = new ObjectMapper();
                System.out.println(mapper.writeValueAsString(result));
                
            } catch (Exception e) {
                System.err.println("Batch processing error: " + e.getMessage());
                System.exit(1);
            }
        } else {
            System.out.println("Usage: java -jar quantrisk.jar --portfolio portfolio.csv");
            System.exit(1);
        }
    }
    
    // Helper methods for printing results
    private static void printVaRResults(VaRResult result) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 VALUE-AT-RISK RESULTS");
        System.out.println("=".repeat(50));
        System.out.printf("Portfolio Value:     $%,.2f%n", result.getPortfolioValue());
        System.out.printf("VaR (%,.1f%% conf):    $%,.2f%n", result.getConfidence() * 100, result.getVaR());
        System.out.printf("VaR Percentage:      %.2f%%%n", result.getVaRPercentage());
        System.out.printf("Expected Shortfall:  $%,.2f%n", result.getExpectedShortfall());
        System.out.printf("Time Horizon:        %d days%n", result.getTimeHorizon());
        System.out.printf("Volatility:          %.2f%%%n", result.getVolatility() * 100);
        System.out.println("=".repeat(50));
    }
    
    private static void printStressTestResults(StressTestResult result) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔥 STRESS TEST RESULTS");
        System.out.println("=".repeat(50));
        System.out.printf("Scenario:            %s%n", result.getScenario());
        System.out.printf("Portfolio Loss:      $%,.2f%n", result.getLoss());
        System.out.printf("Loss Percentage:     %.2f%%%n", result.getLossPercentage());
        System.out.printf("Worst Asset:         %s (%.2f%%)%n", 
                         result.getWorstAsset(), result.getWorstAssetLoss());
        System.out.println("=".repeat(50));
    }
    
    private static void printCorrelationMatrix(String[] symbols, double[][] matrix) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔗 CORRELATION MATRIX");
        System.out.println("=".repeat(60));
        
        // Print header
        System.out.printf("%8s", "");
        for (String symbol : symbols) {
            System.out.printf("%8s", symbol);
        }
        System.out.println();
        
        // Print matrix
        for (int i = 0; i < symbols.length; i++) {
            System.out.printf("%8s", symbols[i]);
            for (int j = 0; j < symbols.length; j++) {
                System.out.printf("%8.3f", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("=".repeat(60));
    }
    
    private static void printMonteCarloResults(MonteCarloResult result) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎲 MONTE CARLO RESULTS");
        System.out.println("=".repeat(50));
        System.out.printf("Simulations:         %,d%n", result.getNumSimulations());
        System.out.printf("VaR (95%%):           $%,.2f%n", result.getVaR95());
        System.out.printf("VaR (99%%):           $%,.2f%n", result.getVaR99());
        System.out.printf("Expected Loss:       $%,.2f%n", result.getExpectedLoss());
        System.out.printf("Worst Case:          $%,.2f%n", result.getWorstCase());
        System.out.printf("Best Case:           $%,.2f%n", result.getBestCase());
        System.out.println("=".repeat(50));
    }
    
    private static Portfolio loadPortfolioFromFile(String filePath) throws Exception {
        // CSV format: symbol,weight,price,quantity
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        Portfolio portfolio = new Portfolio();
        
        for (int i = 1; i < lines.size(); i++) { // Skip header
            String[] parts = lines.get(i).split(",");
            String symbol = parts[0].trim();
            double weight = Double.parseDouble(parts[1].trim());
            double price = Double.parseDouble(parts[2].trim());
            int quantity = Integer.parseInt(parts[3].trim());
            
            portfolio.addPosition(symbol, weight, price, quantity);
        }
        
        return portfolio;
    }
    
    private static VaRResult calculateVaRForPortfolio(Portfolio portfolio) throws Exception {
        CompletableFuture<Map<String, double[]>> dataFuture = 
            dataFetcher.fetchReturnsAsync(portfolio.getSymbols());
        Map<String, double[]> returns = dataFuture.get(30, TimeUnit.SECONDS);
        
        return riskCalc.calculatePortfolioVaR(returns, portfolio.getWeights(), 
                                            portfolio.getTotalValue(), 0.95, 1);
    }
    
    private static void exportResults() {
        System.out.println("\n💾 EXPORT RESULTS");
        System.out.println("-".repeat(40));
        System.out.println("Feature coming soon: Export to CSV/JSON/Excel");
    }
    
    private static void batchProcessing() {
        System.out.println("\n⚡ BATCH PROCESSING");
        System.out.println("-".repeat(40));
        System.out.println("Example batch usage:");
        System.out.println("java -jar quantrisk.jar --portfolio portfolio.csv");
        System.out.println("Outputs JSON for automation/CI integration");
    }
}

// Supporting Classes

class RiskCalculator {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public VaRResult calculatePortfolioVaR(Map<String, double[]> returns, double[] weights, 
                                         double portfolioValue, double confidence, int days) {
        // Calculate portfolio returns
        double[] portfolioReturns = calculatePortfolioReturns(returns, weights);
        
        // Sort returns for percentile calculation
        double[] sortedReturns = portfolioReturns.clone();
        Arrays.sort(sortedReturns);
        
        // Calculate VaR
        int varIndex = (int) ((1 - confidence) * sortedReturns.length);
        double varReturn = sortedReturns[varIndex];
        double var = -varReturn * portfolioValue * Math.sqrt(days);
        
        // Calculate Expected Shortfall (average of losses beyond VaR)
        double expectedShortfall = 0;
        for (int i = 0; i <= varIndex; i++) {
            expectedShortfall += sortedReturns[i];
        }
        expectedShortfall = -expectedShortfall / (varIndex + 1) * portfolioValue * Math.sqrt(days);
        
        // Calculate volatility
        double meanReturn = Arrays.stream(portfolioReturns).average().orElse(0);
        double variance = Arrays.stream(portfolioReturns)
            .map(r -> Math.pow(r - meanReturn, 2))
            .average().orElse(0);
        double volatility = Math.sqrt(variance * 252); // Annualized
        
        return new VaRResult(portfolioValue, var, confidence, expectedShortfall, days, volatility);
    }
    
    public StressTestResult stress2008Crisis(String[] symbols, double portfolioValue) {
        // Historical 2008 crisis shocks
        Map<String, Double> shocks = Map.of(
            "SPY", -0.37, "QQQ", -0.42, "IWM", -0.34,
            "AAPL", -0.56, "MSFT", -0.44, "GOOGL", -0.65
        );
        return applyStressShocks(symbols, portfolioValue, shocks, "2008 Financial Crisis");
    }
    
    public StressTestResult stressCovid2020(String[] symbols, double portfolioValue) {
        // COVID-19 March 2020 crash
        Map<String, Double> shocks = Map.of(
            "SPY", -0.34, "QQQ", -0.25, "IWM", -0.41,
            "AAPL", -0.17, "MSFT", -0.20, "GOOGL", -0.21
        );
        return applyStressShocks(symbols, portfolioValue, shocks, "COVID-19 Crash");
    }
    
    public StressTestResult customStressTest(String[] symbols, double portfolioValue, double shock) {
        Map<String, Double> shocks = new HashMap<>();
        for (String symbol : symbols) {
            shocks.put(symbol, shock);
        }
        return applyStressShocks(symbols, portfolioValue, shocks, "Custom Stress");
    }
    
    private StressTestResult applyStressShocks(String[] symbols, double portfolioValue, 
                                             Map<String, Double> shocks, String scenario) {
        double totalLoss = 0;
        String worstAsset = "";
        double worstLoss = 0;
        
        for (String symbol : symbols) {
            double shock = shocks.getOrDefault(symbol, -0.20); // Default -20%
            double assetLoss = portfolioValue * shock / symbols.length; // Equal weights assumed
            totalLoss += assetLoss;
            
            if (Math.abs(shock) > Math.abs(worstLoss)) {
                worstLoss = shock;
                worstAsset = symbol;
            }
        }
        
        return new StressTestResult(scenario, -totalLoss, -totalLoss/portfolioValue * 100, 
                                  worstAsset, worstLoss * 100);
    }
    
    public double[][] calculateCorrelationMatrix(Map<String, double[]> returns, String[] symbols) {
        int n = symbols.length;
        double[][] correlations = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    correlations[i][j] = 1.0;
                } else {
                    correlations[i][j] = calculateCorrelation(
                        returns.get(symbols[i]), returns.get(symbols[j]));
                }
            }
        }
        
        return correlations;
    }
    
    public MonteCarloResult runMonteCarlo(String[] symbols, double portfolioValue, int numSims) {
        Random random = new Random();
        double[] simulatedReturns = new double[numSims];
        
        // Simplified Monte Carlo - normally distributed returns
        for (int i = 0; i < numSims; i++) {
            double portfolioReturn = 0;
            for (String symbol : symbols) {
                // Simulate return with volatility 
                double assetReturn = random.nextGaussian() * 0.02; // 2% daily vol
                portfolioReturn += assetReturn / symbols.length; // Equal weights
            }
            simulatedReturns[i] = portfolioReturn * portfolioValue;
        }
        
        Arrays.sort(simulatedReturns);
        
        double var95 = -simulatedReturns[(int)(0.05 * numSims)];
        double var99 = -simulatedReturns[(int)(0.01 * numSims)];
        double expectedLoss = -Arrays.stream(simulatedReturns).average().orElse(0);
        double worstCase = -simulatedReturns[0];
        double bestCase = -simulatedReturns[numSims - 1];
        
        return new MonteCarloResult(numSims, var95, var99, expectedLoss, worstCase, bestCase);
    }
    
    private double[] calculatePortfolioReturns(Map<String, double[]> returns, double[] weights) {
        String firstSymbol = returns.keySet().iterator().next();
        int length = returns.get(firstSymbol).length;
        double[] portfolioReturns = new double[length];
        
        for (int i = 0; i < length; i++) {
            double portfolioReturn = 0;
            int assetIndex = 0;
            for (String symbol : returns.keySet()) {
                portfolioReturn += returns.get(symbol)[i] * weights[assetIndex++];
            }
            portfolioReturns[i] = portfolioReturn;
        }
        
        return portfolioReturns;
    }
    
    private double calculateCorrelation(double[] x, double[] y) {
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        int n = Math.min(x.length, y.length);
        
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        return denominator == 0 ? 0 : numerator / denominator;
    }
}

class MarketDataFetcher {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public CompletableFuture<Map<String, double[]>> fetchReturnsAsync(List<String> symbols) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, double[]> returns = new ConcurrentHashMap<>();
            
            List<CompletableFuture<Void>> futures = symbols.stream()
                .map(symbol -> CompletableFuture.runAsync(() -> {
                    double[] symbolReturns = generateSyntheticReturns(symbol, 252); // 1 year
                    returns.put(symbol, symbolReturns);
                }, executor))
                .toList();
                
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return returns;
        });
    }
    
    private double[] generateSyntheticReturns(String symbol, int days) {
        Random random = new Random(symbol.hashCode()); // Consistent data per symbol
        double[] returns = new double[days];
        
        // Generate realistic returns with volatility clustering
        double volatility = 0.015 + random.nextDouble() * 0.015; // 1.5-3% daily vol
        
        for (int i = 0; i < days; i++) {
            returns[i] = random.nextGaussian() * volatility;
        }
        
        return returns;
    }
}

// Result Classes
class VaRResult {
    private final double portfolioValue;
    private final double vaR;
    private final double confidence;
    private final double expectedShortfall;
    private final int timeHorizon;
    private final double volatility;
    
    public VaRResult(double portfolioValue, double vaR, double confidence, 
                    double expectedShortfall, int timeHorizon, double volatility) {
        this.portfolioValue = portfolioValue;
        this.vaR = vaR;
        this.confidence = confidence;
        this.expectedShortfall = expectedShortfall;
        this.timeHorizon = timeHorizon;
        this.volatility = volatility;
    }
    
    public double getPortfolioValue() { return portfolioValue; }
    public double getVaR() { return vaR; }
    public double getVaRPercentage() { return vaR / portfolioValue * 100; }
    public double getConfidence() { return confidence; }
    public double getExpectedShortfall() { return expectedShortfall; }
    public int getTimeHorizon() { return timeHorizon; }
    public double getVolatility() { return volatility; }
}

class StressTestResult {
    private final String scenario;
    private final double loss;
    private final double lossPercentage;
    private final String worstAsset;
    private final double worstAssetLoss;
    
    public StressTestResult(String scenario, double loss, double lossPercentage, 
                          String worstAsset, double worstAssetLoss) {
        this.scenario = scenario;
        this.loss = loss;
        this.lossPercentage = lossPercentage;
        this.worstAsset = worstAsset;
        this.worstAssetLoss = worstAssetLoss;
    }
    
    public String getScenario() { return scenario; }
    public double getLoss() { return loss; }
    public double getLossPercentage() { return lossPercentage; }
    public String getWorstAsset() { return worstAsset; }
    public double getWorstAssetLoss() { return worstAssetLoss; }
}

class MonteCarloResult {
    private final int numSimulations;
    private final double vaR95;
    private final double vaR99;
    private final double expectedLoss;
    private final double worstCase;
    private final double bestCase;
    
    public MonteCarloResult(int numSimulations, double vaR95, double vaR99, 
                          double expectedLoss, double worstCase, double bestCase) {
        this.numSimulations = numSimulations;
        this.vaR95 = vaR95;
        this.vaR99 = vaR99;
        this.expectedLoss = expectedLoss;
        this.worstCase = worstCase;
        this.bestCase = bestCase;
    }
    
    public int getNumSimulations() { return numSimulations; }
    public double getVaR95() { return vaR95; }
    public double getVaR99() { return vaR99; }
    public double getExpectedLoss() { return expectedLoss; }
    public double getWorstCase() { return worstCase; }
    public double getBestCase() { return bestCase; }
}

class Portfolio {
    private final Map<String, Double> weights = new HashMap<>();
    private final Map<String, Double> prices = new HashMap<>();
    private final Map<String, Integer> quantities = new HashMap<>();
    
    public void addPosition(String symbol, double weight, double price, int quantity) {
        weights.put(symbol, weight);
        prices.put(symbol, price);
        quantities.put(symbol, quantity);
    }
    
    public List<String> getSymbols() { return new ArrayList<>(weights.keySet()); }
    
    public double[] getWeights() {
        return weights.values().stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    public double getTotalValue() {
        return prices.entrySet().stream()
            .mapToDouble(entry -> entry.getValue() * quantities.get(entry.getKey()))
            .sum();
    }
}
