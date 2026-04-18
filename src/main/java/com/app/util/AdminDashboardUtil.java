package com.app.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Component;

@Component
public class AdminDashboardUtil {

	// ------------------ PIE CHART ------------------
	public void generatePie(String path, List<Object[]> list) {

		// 1. Create dataset
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

		for (Object[] ob : list) {
			String key = ob[0].toString(); // Label
			Double value = Double.valueOf(ob[1].toString()); // Count
			dataset.setValue(key, value);
		}

		// 2. Create Chart
		JFreeChart chart = ChartFactory.createPieChart("ADMIN SLOTS DATA", dataset, true, // legend
				true, // tooltips
				false // URLs
		);

		// 3. Save as Image
		try {
			ChartUtils.saveChartAsJPEG(new File(path + File.separator + "adminA.jpg"), chart, 500, 400);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ------------------ BAR CHART ------------------
	public void generateBar(String path, List<Object[]> list) {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (Object[] ob : list) {
			String category = ob[0].toString(); // Slot Name
			Double value = Double.valueOf(ob[1].toString()); // Count

			dataset.setValue(value, "Slots", category);
		}

		// Create Bar Chart
		JFreeChart chart = ChartFactory.createBarChart("ADMIN SLOTS DATA", "SLOT", "COUNT", dataset);

		try {
			ChartUtils.saveChartAsJPEG(new File(path + File.separator + "adminB.jpg"), chart, 500, 400);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}