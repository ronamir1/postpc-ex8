package huji.postpc.y2021.multicalculator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CalculationHolderTest {
    @Test
    public void calculationHolder_initialize_calcs_in_progress()
    {
        CalculationHolder calculationHolder = new CalculationHolder();
        calculationHolder.addCalc(10);
        Calculation calculation = calculationHolder.calculations.get(0);
        assertTrue(calculation.inProgress);
    }

    @Test
    public void calculationHolder_calcs_are_in_order()
    {
        CalculationHolder calculationHolder = new CalculationHolder();
        calculationHolder.addCalc(10);
        calculationHolder.addCalc(5);
        Calculation calculation = calculationHolder.calculations.get(0);
        assertEquals(5, calculation.root, 0.0);
    }

    @Test
    public void calculationHolder_when_calc_is_done_its_location_updates()
    {
        CalculationHolder calculationHolder = new CalculationHolder();
        calculationHolder.addCalc(10);
        calculationHolder.addCalc(5);
        Calculation calculation = calculationHolder.calculations.get(0);
        assertEquals(5, calculation.root, 0.0);
        calculationHolder.completedCalc(calculation);
        calculation = calculationHolder.calculations.get(0);
        assertEquals(10, calculation.root, 0.0);
    }
}