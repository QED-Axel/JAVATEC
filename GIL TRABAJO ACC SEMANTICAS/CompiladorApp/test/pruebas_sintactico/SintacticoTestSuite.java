/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:        # Suite JUnit  para pruebas del Analizador Sintactico                 
 *:                           
 *: Archivo       : SinctacticoTestSuite.java
 *: Autor         : Fernando Gil   
 *: 
 *: Fecha         : 12/Oct/2024
 *: Compilador    : Java JDK 17,  JUnit 4.13.2 y Hamcrest 1.3
 *: Descripción   : Suite de casos de prueba correctos e incorrectos para validar
 *:                 el Analizador Sintactico de lenguaje JavaTec.   
 *:                 Esta suite se forma con los casos de prueba siuientes:
 *:                     1. SintacticoOKTest.java
 *:                     2. SintacticoErrTest.java
 *:           	      
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 12/Oct/2024 FGil               -Suite para lenguaje JavaTec 2024  
 *:-----------------------------------------------------------------------------
 */

package pruebas_sintactico;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author FGIL.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses ( {pruebas_sintactico.SintacticoOKTest.class, 
                       pruebas_sintactico.SintacticoErrTest.class })
public class SintacticoTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
