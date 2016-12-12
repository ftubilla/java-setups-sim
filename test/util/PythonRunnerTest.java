package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Test;

public class PythonRunnerTest {

    @Test
    public void test() {

        try {
            int number1 = 10;
            int number2 = 32;

            String dir = System.getProperty("user.dir");
            ProcessBuilder pb = new ProcessBuilder("/Users/ftubilla/Library/Enthought/Canopy_64bit/User/bin/python",
                    dir + "/python/python_test.py", "" + number1, "" + number2);
            Process p = pb.start();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));  
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            System.out.println(err.readLine());
            System.out.println("value is : " + in.readLine());
            System.out.println("value is : " + in.readLine());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
