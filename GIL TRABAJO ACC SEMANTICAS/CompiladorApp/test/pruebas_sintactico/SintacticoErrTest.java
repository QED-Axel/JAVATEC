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
 *: Archivo       : SinctacticoErrTest.java
 *: Autor         : Fernando Gil   
 *: 
 *: Fecha         : 12/Oct/2024
 *: Compilador    : Java JDK 17,  JUnit 4.13.2 y Hamcrest 1.3
 *: Descripción   : Casos de prueba con programas con error sintactico escritos en 
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
import org.junit.Ignore;

/**
 *
 * @author FGIL.0
 */
public class SintacticoErrTest {
    Compilador cmp = new Compilador ();
    
    public SintacticoErrTest() {
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
    public void emparejarTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Error en la linea 1, falta el identificador de programa
        programas.add ( """
        public class { public static void main ( string args [] ) { } }                 
        """ );
        
        // #2 - Error en la linea 1, falta el { despues de Prueba01.
        programas.add ( """
        public class Prueba01  public static void main ( string args [] ) { } }         
        """ );
        
        // #3 - Error en la linea 1, falta static en la declaracion del main.
        programas.add ( 
      """
        public class Prueba01 { public void main ( string args [] ) { } }          
        """ );

        // #4 - Error en la linea 1, falta la palabra main
        programas.add ( 
      """
        public class Prueba01 { public static void  ( string args [] ) { } }    
        """ );

        // #5 - Error en la linea 1, falta args despues string
        programas.add ( 
      """
        public class Prueba01 { public static void main ( string  [] ) { } } 
        """ );        

        // #6 - Error en la linea 1, falta ) que cierra los argumentos del main
        programas.add ( 
      """
        public class Prueba01 { public static void main ( string args []  { } }
        """ );
        
        // #7 - Error en la linea 2,3,4 y , falta el nombre del metodo
        programas.add ( 
      """
          public class Prueba01 { 
          public static void   {}
          public static int    {}
          public static float  {}
          public static string {}
          public static void main  ( string args [] ) { } 
        }
        """ );

        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* emparejarTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar al menos 1 error y el primer error debe ser de [emparejar]
            int totErrores = cmp.getTotErrores ( Compilador.ERR_SINTACTICO );
            String primerMensError = cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO );
            assertTrue ( "emparejarTest #" + i, 
                 totErrores    > 0 );
            assertTrue ( "emparejarTest #" + i, 
                 primerMensError.toUpperCase().contains("[EMPAREJAR]" ) );
        }
    }
  
    //--------------------------------------------------------------------------
    
    @Test
    public void simboloInicialTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Error en la linea 1, no se permite un programa en blanco
        programas.add ( "" );
        
        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* siboloInicialTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar al menos 1 error y el primer error debe ser de [emparejar]
            assertTrue ( "siboloInicialTest #" + i, 
                    cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) > 0 );
//          (12Oct2024) Anulado este bloque temporalmente
//          assertTrue ( "siboloInicialTest #" + i, 
//                cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO )
//                        .toUpperCase().contains("[CLASE]" ) );
        }
    }  
    
    //--------------------------------------------------------------------------
    
    @Test
    public void decVariablesTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Error en la linea 3, se esperaba un tipo de dato
        programas.add ( 
      """
        public class Prueba01
        {
          public static a,  b;
          public static void main ( string args [] ) {
          }  
        }
        """         
        );
        
        // #2 - Error en la linea 3, falta una , entra a y b.
        programas.add ( 
      """
        public class Prueba01
        {
          public static int a  b;
          public static void main ( string args [] ) {
          }  
        }
        """         
        );        
        
        // #3 - Error en la linea 3, falta cerrar el ]
        programas.add ( 
      """
        public class Prueba01
        {
          public static int a, b [10;
          public static void main ( string args [] ) {
          }  
        }
        """         
        ); 

        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* decVariablesTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar al menos 1 error y el primer error debe ser de [emparejar]
            assertTrue ( "decVariablesTest #" + i, 
                    cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) > 0 );
//          (12Oct2024) Anulado este bloque temporalmente
//          assertTrue ( "decVariablesTest #" + i, 
//                cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO )
//                        .toUpperCase().contains("[TIPO]" ) );
        }
    }     
    
    //--------------------------------------------------------------------------
    @Test
    public void decMetodosTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Error en la linea 3, el nombre del metodo no puede ser int
        programas.add ( 
      """
        public class Prueba01
        {
          public static void int () {
          }

          public static void main ( string args [] ) {
          }  
        }
        """         
        );
        
        // #2 - Error en la linea 3, el tipo del argumento a no puede ser void
        programas.add ( 
      """
        public class Prueba01
        {
          public static void imprimir ( void a ) {
          }

          public static void main ( string args [] ) {
          }  
        }
        """         
        );        
       
        // #3 - Error en la linea 3, faltó el nombre del argumento
        programas.add ( 
      """
        public class Prueba01
        {
          public static void imprimir ( string  ) {
          }

          public static void main ( string args [] ) {
          }  
        }
        """         
        ); 
        
        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* decMetodosTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar al menos 1 error y el primer error debe ser de [emparejar]
            assertTrue ( "decMetodosTest #" + i, 
                    cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) > 0 );
//          (12Oct2024) Anulado este bloque temporalmente
//          assertTrue ( "decMetodosTest #" + i, 
//                cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO )
//                        .toUpperCase().contains("[TIPO_METODO]" ) );
        }
    }     
    
    //--------------------------------------------------------------------------    
    
    @Test
    public void expresionTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Error en la linea 4, se esperaba una expresion
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            suma = ;
          }  
        }
        """         
        );
 
        // #2 - Error en la linea 4, se esperaba una expresion
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            suma = ();
          }  
        }
        """         
        );
        
        // #3 - Error en la linea 4, se esperaba una expresion
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            suma = 2 * ;
          }  
        }
        """         
        );
  
        // #4 - Error en la linea 4, se esperaba una expresion
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            suma = 2 sumar ;
          }  
        }
        """         
        );
        
        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* expresionTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar al menos 1 error y el primer error debe ser de [emparejar]
            assertTrue ( "expresionTest #" + i, 
                    cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) > 0 );
//          (12Oct2024) Anulado este bloque temporalmente
//           assertTrue ( "expresionTest #" + i, 
//                cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO )
//                        .toUpperCase().contains("[EXPRESION]" ) );
        }
    }     

    //--------------------------------------------------------------------------  
    
    @Test
    public void invocacionMetodosTest () {
        ArrayList<String> programas = new ArrayList<> ();
        
        // #1 - Error en la linea 4, void no es un metodo
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            void ();
          }  
        }
        """         
        );
 
        // #2 - Error en la linea 4, no se esperaba ahora
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            imprimir ahora ();
          }  
        }
        """         
        );
        
        // #3 - Error en la linea 4, falta la , entre el 0 y 1
        programas.add ( 
      """
        public class Prueba01
        {
          public static void main ( string args [] ) {
            desplegar ( 0 1  );
          }  
        }
        """         
        );
  
      
        int i = 0;
        // Por cada uno de los programas de prueba...
        for ( String programa : programas ) { 
            i++;
            System.out.println ( "********* invocacionMetodosTest #" + i + " *********" );
            
            // Ejecutar el Lexico y Sintactico 
            cmp.analizarLexico (programa );
            cmp.analizarSintaxis();

            // Debe contabilizar al menos 1 error y el primer error debe ser de [emparejar]
            assertTrue ( "invocacionMetodosTest #" + i, 
                    cmp.getTotErrores ( Compilador.ERR_SINTACTICO ) > 0 );
//          (12Oct2024) Anulado este bloque temporalmente
//           assertTrue ( "invocacionMetodosTest #" + i, 
//                cmp.getPrimerMensError ( Compilador.ERR_SINTACTICO )
//                        .toUpperCase().contains("[EXPRESION]" ) );
        }
    }     

    //--------------------------------------------------------------------------     
}
