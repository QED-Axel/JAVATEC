/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:        # Casos de prueba JUnit  para el Analizador Sintactico                 
 *:                           
 *: Archivo       : SinctacticoOKTest.java
 *: Autor         : Fernando Gil   
 *: 
 *: Fecha         : 12/Oct/2024
 *: Compilador    : Java JDK 17,  JUnit 4.13.2 y Hamcrest 1.3
 *: Descripción   : Casos de prueba con programas correctamente escritos en 
 *:                 lenguajes JavaTec.   
 *:           	      
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 12/Oct/2024 FGil               -Se crearon los casos de prueba para JavaTec 2024  
 *:-----------------------------------------------------------------------------
 */

package pruebas_sintactico;

import compilador.Compilador;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author FGIL.0
 */
public class SintacticoOKTest {
    Compilador cmp = new Compilador ();
    
    public SintacticoOKTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    //--------------------------------------------------------------------------
    
    @Test
    public void programasOKTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static void main ( string args [] ) {
          }
        }
        """ );
        
        // #2 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static float a,  b;
          public static int   i;
          public static string c1, c2;

          public static void main ( string args [] ) {
          }  
        }
        """ );

        // #3 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static void imprimir () {
          }

          public static string datos () {
          }

          public static int suma ( int q ) {
          }

          public static float promediar ( float a, int b, float c ) {
          }

          public static void main ( string args [] ) {
          }  
        }
        """ );
        
        // #4 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static int x, y;
          public static float a, b, c;
          public static int r, p;
          public static string c1, c2;

          public static void imprimir () {
          }

          public static int borrar () {
          }

          public static string insertar () {
          }

          public static int suma ( int q ) {
          }

          public static float promediar ( float aa, int bb, float cc ) {
          }

          public static void main ( string args [] ) {
          }  
        }
        """ );

        // #5 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            a = 0;
            a = b;
            a = b + c;
            suma = 0.0;
            suma = aa + b + suma;
            resultado = ( ( 2 * aa ) + b ) * c; 
          }  
        }
        """ );

        // #6 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            suma = sumar ();
            suma = 2 * sumar () + restar ( a, b );
          }  
        }
        """ );
        
        // #7 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static float promediar ( float a, int b, float c ) {
            if ( a + b <= 2 * c ) { 
              suma = 0.0;
              suma = a + b + c;
            } else {
              suma = a + b + c;
              resultado = ( 2 * a ) + b * c; 
            }
          }

          public static void main ( string args [] ) {
          }  
        }
        """ );       
        
        // #8 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static float promediar ( float a, int b, float c ) {
            while ( a + b <= 2 * c ) { 
              if ( a == 0 ) {
                suma = 0.0;
              } else {
                suma = a + b + c;
              }
            }
          }

          public static void main ( string args [] ) {
          }  
        }
        """ ); 

        // #9 - Programa sin errores
        programas.add ( """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            imprimir ();
            desplegar ( 0 );
            mostrar ( a, a + 1, 2.0 * ( a + 1 ) );
          }  
        }
        """ ); 

       
        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* programasOKTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar 0 errores y el primer error registrado debe ser "" 
            assertEquals ( "programasOKTest #" + i, 
                    0, cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) );
            assertEquals ( "programasOKTest #" + i, 
                    "", cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO ) );
        }
    }  
    
    //--------------------------------------------------------------------------
  
}
