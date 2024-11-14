/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *:
 *: 26/sep/2024 LBarranco & SLopez  -Se implemento javatec, se tuvo que agregar 
 *:                                 los siguientes procedures:
 *:                                 lista_identificadores, lista_proposiciones, 
 *:                                 proposicion metodo, lista_expresiones, 
 *:                                 expresion, expresion_prima, factor.
 *:
 *: 27/sep/2024 LBarranco & SLopez  -Se implemento el retroceder() para evitar errores
 *:
 *: 29/sep/2024 Lbarranco & Slopez – Se corregieron: proposicion_compuesta, lista_parametros_prima,
 *:                                 declaracion_metodo,termino_prima y metodo_principal
 *:-----------------------------------------------------------------------------
 */
package compilador;

import general.Linea_BE;
import javax.swing.JOptionPane;

public class SintacticoSemantico {
    
    //Constantes
    public static String VACIO = "vacio";
    public static String ERROR_TIPO = "error_tipo";

    private Compilador cmp;
    private boolean analizarSemantica = false;
    private String preAnalisis;
    //variables para resolver el problema de la grmatica
    private boolean retroceso;// bandera que indica si se realizo un retroceso en el buffer de entrada
    private int ptr;// apuntador auxiliar a una localidad del buffer de entrada

    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //
    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica
    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *
        clase();

    }

    //--------------------------------------------------------------------------
    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;
        } else {
            errorEmparejar(t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
    private void errorEmparejar(String _token, String _lexema, int numLinea) {
        String msjError = "[emparejar] ";  // (2023.1) : 13Sep2023 : FGil
                                           // Se cambió la inicializacion.

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + (_lexema.equals("$") ? "fin de archivo" : _lexema)
                + ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico
    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    // Fin de error
    //--------------------------------------------------------------------------
    // Metodo para retroceder el simbolo de preAnalisis en el buffer de entrada
    // a la posicion previamente guardada en ptr
    private void retroceso() {
        cmp.be.setPrt(ptr);
        preAnalisis = cmp.be.preAnalisis.complex;
        retroceso = true;
    }

    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------
    ////////////////////////////////////////////////////
//primeros (clase) = {‘public’}
// Implementado por: Luis Ernesto Barranco (21130876)
    private void clase() {
        if (preAnalisis.equals("public")) {
            /* clase → public class  id  {   declaraciones
declaraciones_metodos    metodo_principal }*/
            emparejar("public");
            emparejar("class");
            emparejar("id");
            emparejar("{");
            declaraciones();
            declaraciones_metodos();
            metodo_principal();
            emparejar("}");
        } else {
            error("[clase] Error al iniciar la clase se encontro (" + preAnalisis + ") se esperaba (public). Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }
    
    private void lista_identificadores(Atributos LI) {
        //Variables Locales 
        Linea_BE id = new Linea_BE();
        Atributos LIP = new Atributos();
        Atributos dimension = new Atributos();
        if (preAnalisis.equals("id")) {
            // lista_identificadores -> id dimension lista_identificadores’
            id = cmp.be.preAnalisis; //Se salva los atributos de ID
            emparejar("id");
            dimension(dimension);
            //Accion Semantica 12 
            if ( analizarSemantica ){
                if ( cmp.ts.buscaTipo(id.entrada) == null ){
                    if ( dimension.esArreglo == true ){
                        cmp.ts.anadeTipo(id.entrada, "array(0.." + dimension.longitud + "," + LI.h + ")");
                    }else{
                        cmp.ts.anadeTipo(id.entrada, LI.h);
                    }
                    LI.h = VACIO;
                }else{
                    LI.h = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_identificadores] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 12
            //Accion Semantica 13
            if ( analizarSemantica ){
                LIP.h = LI.h;
            }
            //Fin accion Semantica 13
            lista_identificadores_prima(LIP);
            //Accion Semantica 14
            if ( analizarSemantica ){
                if ( LIP.tipo.equals(VACIO) && LI.h.equals(VACIO)){
                    LI.tipo = VACIO;
                }else{
                    LI.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_identificadores 2] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 14
        } else {
            error("[lista_identificadores] - " //CAMBIAR EL MENSAJE
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void lista_identificadores_prima(Atributos LIP) {
        //Variables Locales
        Atributos dimension = new Atributos();
        Atributos LIP1 = new Atributos();
        if (preAnalisis.equals(",")) {

            // lista_identificadores’ -> , id  dimension  lista_identificadores’
            emparejar(",");
            emparejar("id");
            dimension(dimension);
            lista_identificadores_prima(LIP1);

        } else {
            // lista_identificadores’ -> empty 
        }
    }

    private void declaraciones(Atributos DE) {
        //Variables Locales
        Atributos tipo = new Atributos();
        Atributos DE1 = new Atributos();
        Atributos LI = new Atributos();
        //Inicializamos bandera de retroceso en falso 
        retroceso = false;
        if (preAnalisis.equals("public")) {
            //declaraciones -> public static tipo lista_identificadores ; declaraciones
            ptr = cmp.be.getptr(); //Se guarda la posicion actual del preAnalisis
            emparejar("public");
            emparejar("static");
            tipo(tipo);
            //Accion Semantica 3
            if ( analizarSemantica ){
                LI.h = tipo.tipo;
            }
            //Fin accion Semantica 3
            if ( ! retroceso ){
                lista_identificadores(LI);
                if ( preAnalisis.equals( ";" )){
                    //Si es punto y coma se trata de una sentencia de declaracion de variables
                    emparejar(";");
                    declaraciones(DE1);
                }else{
                    retroceso();
                }
            }
            //Accion Semantica 4
            if ( analizarSemantica ){
                if ( LI.tipo.equals(VACIO) && DE1.tipo.equals(VACIO)){
                    DE.tipo = VACIO;
                }else{
                    DE.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[declaraciones] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 4
        } else {
            //declaraciones -> ϵ
            //Accion Semantica 5 
            if ( analizarSemantica ){
                DE.tipo = VACIO;
            }
            //Fin accion Semantica 5
        }
    }

    private void tipo(Atributos tipo) {
        //Variables locales
        Atributos TE = new Atributos();
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            // tipo -> tipo_estandar
            tipo_estandar(TE);
            //Accion Semantica 6 
            if ( analizarSemantica ){
                tipo.tipo = TE.tipo;
            }
            //Fin accion Semantica 6
        } else if ( preAnalisis.equals( "void" )){
            retroceso();
        } else {
            error(" [tipo] tipo de dato no reconocido (int, float, string) ");
        }

    }
    
    
    
///////////////////////////////////////////////////

//———————————————————————————————————————————————————————————————
//lista_identificadores → id  dimension  lista_identificadores’
// Primeros lista_identificadores = { id }
    private void lista_identificadores() {
        if (preAnalisis.equals("id")) {
            // lista_identificadores’ -> id  dimension  lista_identificadores’
            emparejar("id");
            Atributos dimension = null;
            dimension(dimension);
            lista_identificadores_prima();
        } else {
            error("[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

///////////////////////////////////////////////////////
//-----------------------------------------------------------------
// Código en java
// IMPLEMENTADO POR: Edgar Manuel Carrillo Muruato 21130864
// Primeros lista_identificadores_prima = { , } U { empty }
    private void lista_identificadores_prima() {
        if (preAnalisis.equals(",")) {
            // lista_identificadores’ -> , id  dimension  lista_identificadores’
            emparejar(",");
            emparejar("id");
            Atributos dimension = null;
            dimension(dimension);
            lista_identificadores_prima();
        } else {
            // lista_identificadores’ -> empty
        }
    }

//---------------------------------------------------------------
// IMPLEMENTACIÓN
//Implementado por : Paulina Jaqueline Castañeda Villalobos (21130850) //PRIMEROS (declaraciones) = {'public'} U {'ε'}
    private void declaraciones() {
        retroceso = false;
        if (preAnalisis.equals("public")) {
            //declaraciones -> public static tipo lista_identificadores ;
            //declaraciones();
            ptr = cmp.be.getptr();
            emparejar("public");
            emparejar("static");
            tipo();
            if (!retroceso) {//si no hubo retroceso continuamos...
                lista_identificadores();
                if (preAnalisis.equals(";")) {
                    emparejar(";");
                    declaraciones();
                } else {
                    retroceso();
                }
            }
        } else {
            //declaraciones -> ε 
        }
    }

//-----------------------------------------------------------------------------
    // Codigo implementado en JAVA
    //  Primeros ( tipo ) = First(tipo_estandar) = { entero,  float,  string }
    //
    //Implementado por: Gael Costilla Garcia (21130923)
    private void tipo() {
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            Atributos TE = null;
            // tipo -> tipo_estandar
            tipo_estandar(TE);
        } else if (preAnalisis.equals("void")) {
            // si el tipo es void corresponde a la declaracion de un metodo.
            retroceso();
        } else {
            error(" [tipo] tipo de dato no reconocido (int, float, string), se encontro (" + preAnalisis + ") Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------------------------
// CODIGO EN JAVA;
// Implementado por Jose Eduardo Gijon Mora  (21130883)
// PRIMEROS (tipo_estandar) = {'int'} U {'float'} U {'string'}
    private void tipo_estandar(Atributos TE) {
        if (preAnalisis.equals("int")) {
            // tipo_estandar -> int
            emparejar("int");
        } else if (preAnalisis.equals("float")) {
            // tipo_estandar -> float
            emparejar("float");
        } else if (preAnalisis.equals("string")) {
            // tipo_estandar -> string
            emparejar("string");
        } else {
            error("[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato estándar. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//-----------------------------------------------------------------------------
// Diseñado por: Layla Vanessa González Martínez 21130868
// PROCEDURE dimension
// Primeros (dimension) = {[} U {empty}
    private void dimension(Atributos dimension) {
        if (preAnalisis.equals("id")) {
            // dimension -> [ num ]
            emparejar("[");
            emparejar("num");
            emparejar("]");
        } else {
            // dimension -> empty
        }
    }

//—-------------------------------------------------------------------------- //Implementado por: ANA SOFIA GONZALEZ VALERIO
    private void declaraciones_metodos() {
        retroceso = false;
        if (preAnalisis.equals("public")) {
            ptr = cmp.be.getptr();
            declaracion_metodo();
            if (!retroceso) {
                declaraciones_metodos();
            } else {
                retroceso();
            }
        } else {
//empty }
        }

    }
//------------------------------------------------------------------------------
//

//Implementado por: VALERY ARACELI GUERRERO RODRIGUEZ (21130925)
    private void declaracion_metodo() {//primeros declaracion_metodo = {"public"}
        if (preAnalisis.equals("public")) {
            encab_metodo();
            if (!retroceso) {
                proposicion_compuesta();
            } else {
                retroceso();
            }
        } else {
            error("[declaracion_metodo] El programa debe iniciar con declaracion de variable con la palabra reservada (public) Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }
//------------------------------------------------------

//—--------------------------------------------------------------------------
// Implementado por: Alejandro Huerta Reyna 21130857
    private void encab_metodo() {
        if (preAnalisis.equals("public")) {
            // encab_metodo →  public static  tipo_metodo  id( lista_parametros )
            emparejar("public");
            emparejar("static");
            tipo_metodo();
            if (preAnalisis.equals("id")) {
                emparejar("id");
                emparejar("(");
                lista_parametros();
                emparejar(")");
            } else if (preAnalisis.equals("main")) {
                retroceso();
            }
        } else {
            error("[encab_metodo] - Error de sintaxis "
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//—-----------------------------------------------------------------------------
    // Código implementado en JAVA
// IMPLEMENTADO POR: Diana Laura Juarez Cordova
//Procedure (metodo_principal)
//Primeros (metodo_principal) =
    private void metodo_principal() {
        if (preAnalisis.equals("public")) {
            //metodo_principal → public static void  main ( string  args [ ]  )   proposición_compuesta
            emparejar("public");
            emparejar("static");
            emparejar("void");
            emparejar("main");
            emparejar("(");
            emparejar("string");
            emparejar("args");
            emparejar("[");
            emparejar("]");
            emparejar(")");
            proposicion_compuesta();
        } else {
            error("[metodo_principal] se encontro (" + preAnalisis + ") se esperaba (public), Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }
//—-----------------------------------------------------------------------------

//------------------------------------------------------------------------------
// CODIGO tipo_metodo
// PRIMERO ( tipo_metodo )   = { ‘void’ , ‘int’ , ‘float’ , ‘string’ }
// Implementado por: Marcos Juárez ( 21130852 )
    private void tipo_metodo() {
        if (preAnalisis.equals("void")) {
            // tipo_metodo -> void
            emparejar("void");
        } else if (preAnalisis.equals("int")
                || preAnalisis.equals("float")
                || preAnalisis.equals("string")) {
            Atributos TE = null;
            // tipo_metodo -> tipo_estandar corchetes
            tipo_estandar(TE);
            corchetes();
        } else {
            error("[tipo_metodo] - Tipo de dato incorrecto ( int, float, string ). "
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------
//PRIMEROS(corchetes) = { [ } U { ϵ }
//Implementado por: Jesus Emmanuel Llamas Hernandez - 21130904
    private void corchetes() {
        if (preAnalisis.equals("[")) {
            // corchetes -> []
            emparejar("[");
            emparejar("]");
        } else {
            // corchetes -> ϵ
        }
    }

//-------------------------------------------------------------------------------
    //PROCEDURE lista_parametros
    //Primeros ( lista_parametros ) = { int, float, string } U { empty }
    //Implementado por: Rodrigo Macias Ruiz (21131531)
    private void lista_parametros() {
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            //lista_parametros →  tipo  id  dimension  lista_parametros’  | ϵ
            tipo();
            emparejar("id");
            Atributos dimension = null;
            dimension(dimension);
            lista_parametros_prima();
        } else {
            // lista_parametros -> empty
        }
    }

//-------------------------------------------------------------------------------
    //PROCEDURE lista_parametros 
    //Primeros ( lista_parametros ) = { , } U { empty } 
    //Implementado por: Rodrigo Macias Ruiz (21131531)
    private void lista_parametros_prima() {
        if (preAnalisis.equals(",")) {
            //lista_parametros →  , tipo  id  dimension  lista_parametros’  | ϵ
            emparejar(",");
            tipo();
            emparejar("id");
            Atributos dimension = null;
            dimension(dimension);
            lista_parametros_prima();
        } else {
            // lista_parametros -> empty
        }
    }

    //-------------------------------------------------------------------
//Procedure Proposicion’
//
// PRIMERO (proposicion') = { [ } U { opasig } U { ( } U { empty };
//
// Diseñado por: Manuel Mijares Lara Hola Noloh :), noloh deja de usar el cel
    private void proposicion_prima() {
        if (preAnalisis.equals("[")) {
            //proposicion' -> [expresion] opasig expresion
            emparejar("[");
            expresion();
            emparejar("]");
            emparejar("opasig");
            expresion();
        } else if (preAnalisis.equals("opasig")) {
            //proposicion' -> opasig expresion
            emparejar("opasig");
            expresion();
        } else if (preAnalisis.equals("(")) {
            //proposicion' -> proposicion_metodo
            proposicion_metodo();
        } else {
            //proposicion' -> empty
        }
    }

//-----------------------------------------------------------------------------
//Diseñado por: José Alejandro Martínez Escobedo - 19130939
//PROCEDURE proposicion_compuesta
//primeros proposicion_compuesta = {'{'}
    private void proposicion_compuesta() {
        if (preAnalisis.equals("{")) {
            emparejar("{");
            proposiciones_optativas();
            emparejar("}");
        } else {
            error("[proposicion_compuesta] Se esperaba ({) se encontro: (" + preAnalisis + ") - Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------
// Diseñado por: Rodrigo Mazuca Ramirez
// Procedure proposiciones-optativas
// PRIMERO ( proposiciones-optativas) = RRIMERO ( lista_proposiciones )
// PRIMERO 
// PRIMERO ( prioposicion ) = PRIMERO (proposicion’)
// PRIMERO (proposicion) = { id , if, while, {  } 
    private void proposiciones_optativas() {
        if (preAnalisis.equals("id") || preAnalisis.equals("if") || preAnalisis.equals("while")) {
            lista_proposiciones();
        } else if (preAnalisis.equals("{")) {
            // proposiciones_optativas -> lista_proposiciones
            lista_proposiciones();
        } else {
            //Proposicion_optatica ->empty
        }

    }
//------------------------------------------------------

//———————————————————————————————————————————————————————————————
//lista_proposiciones → proposición    lista_proposiciones   | ϵ
// Primeros lista_proposiciones = { proposición } ∪ { empty }
    private void lista_proposiciones() {
        if (preAnalisis.equals("id") || preAnalisis.equals("if") || preAnalisis.equals("while")||preAnalisis.equals("{")) {
            //lista_proposiciones → proposición    lista_proposiciones   | ϵ
            proposicion();
            lista_proposiciones();
        } else {
            // lista_proposiciones -> empty
        }
    }

//—--------------------------------------------------------------------------
// Implementado por: Humberto Medina Santos (21130862)
    private void proposicion() {
        if (preAnalisis.equals("id")) {
            // proposicion -> id proposicion' ; 
            emparejar("id");
            proposicion_prima();
            emparejar(";");
        } else if (preAnalisis.equals("{")) {
            // proposicion -> proposicion_compuesta
            proposicion_compuesta();
        } else if (preAnalisis.equals("if")) {
            // proposicion -> if ( expresión ) proposición else proposición
            emparejar("if");
            emparejar("(");
            expresion();
            emparejar(")");
            proposicion();
            emparejar("else");
            proposicion();
        } else if (preAnalisis.equals("while")) {
            // proposicion -> while ( expresión ) proposición 
            emparejar("while");
            emparejar("(");
            expresion();
            emparejar(")");
            proposicion();
        } else {
            error("[proposicion] Error en la proposicion Linea:" + cmp.be.preAnalisis.numLinea);
        }
    }

//———————————————————————————————————————————————————————————————
//proposición_metodo→ ( lista_expresiones )  | ϵ
// Primeros proposición_metodo = { lista_expresiones } ∪ { empty }
    private void proposicion_metodo() {
        if (preAnalisis.equals("(")) {
            //proposición_metodo = { lista_expresiones }
            emparejar("(");
            lista_expresiones();
            emparejar(")");
        } else {
            //proposicion_metodo -> empty
        }

    }

//———————————————————————————————————————————————————————————————
//lista_expresiones → expresión    lista_expresiones’  | ϵ
// Primeros lista_expresiones = { expresión } ∪ { empty }
    private void lista_expresiones() {
        if (preAnalisis.equals("id")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion();
            lista_expresiones_prima();
        } else if (preAnalisis.equals("num")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion();
            lista_expresiones_prima();
        } else if (preAnalisis.equals("num.num")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion();
            lista_expresiones_prima();
        } else if (preAnalisis.equals("(")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion();
            lista_expresiones_prima();
        } else {
            // lista_expresiones -> empty    
        }
    }
    //--------------------------------------------------------------------------
    // PROCEDURE lista_expresiones'
    // PRIMERO(lista_expresiones') = { ',' } U { 'empty' }
    // Implementado por: Diego Muñoz Rede (21130893)

    private void lista_expresiones_prima() {
        if (preAnalisis.equals(",")) {
            // lista_expresiones' -> , expresion lista_expresiones'
            emparejar(",");
            expresion();
            lista_expresiones_prima();
        } else {
            /*lista_expresiones' -> empty*/ }
    }

//———————————————————————————————————————————————————————————————
//expresión → expresión_simple  expresion’   |  literal
// Primeros expresion = { expresion_simple } ∪ { literal }
    private void expresion() {
        if (preAnalisis.equals("id")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple();
            expresion_prima();
        } else if (preAnalisis.equals("num")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple();
            expresion_prima();
        } else if (preAnalisis.equals("num.num")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple();
            expresion_prima();
        } else if (preAnalisis.equals("(")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple();
            expresion_prima();
        } else if (preAnalisis.equals("literal")) {
            // expresion -> literal
            emparejar("literal");
        } else {
            error("[expresion] error se esperaba id,num,num.num,(,literal. "
                    + "Se encontro: (" + preAnalisis + ") - Linea: " 
                    + cmp.be.preAnalisis.numLinea);
        }

    }

//———————————————————————————————————————————————————————————————
//expresion’ → oprel   expresión_simple  |  ϵ
// Primeros expresion = { oprel } ∪ { empty }
    private void expresion_prima() {
        if (preAnalisis.equals("oprel")) {
            //expresion_prima → {oprel expresion_simple}
            emparejar("oprel");
            expresion_simple();
        } else {
            // expresion’ -> empty
        }
    }

    //-------------------------------------------------------------------
//PROCEDURE expresion_simple
//Primero (expresion_simple) = PRIMEROS(termino) = {id,num, num.num,(}
//Implementado por: Miriam Alicia Sanchez Cervantes (21130882)
    private void expresion_simple() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {
            termino();
            expresion_simple_prima();
        } else {
            error("[expresion_simple]Tiene que empezar con (id, num, num.num, ( ). Linea: " + cmp.be.preAnalisis.numLinea);
        }

    }

//-----------------------------------------------------------------------------
// Código implementado en Java
// PRIMEROS (expresion_simple_prima) = {'opsuma' } U { 'empty' }
// Implementado por : María Fernanda Torres Herrera (21130859)
    private void expresion_simple_prima() {
        if (preAnalisis.equals("opsuma")) {
            // expresion_simple_prima -> opsuma termino expresion_simple_prima
            emparejar("opsuma");
            termino();
            expresion_simple_prima();
        } else {
            // expresion_simple_prima -> empty
        }
    }

    //-------------------------------------------------------------------------------
// PROCEDURE termino
// PRIMEROS ( termino )  = PRIMEROS ( factor ) 
//                       = { id, num, num.num, ( }
// Implementando por:  Juan Fernando Vaquera Sanchez 21130869
    private void termino() {

        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {

            // termino -> factor termino'
            factor();
            termino_prima();

        } else {

            error("[termino] Falto la definicion del factor al inicio del termino. Linea: "
                    + cmp.be.preAnalisis.numLinea);

        }

    }

//------------------------------------------------------------------------------
//Implementado por: Luis Alejandro Vazquez Saucedo  
//PROCEDURE término’
//PRIMERO(termino’) = {opmult}  
    private void termino_prima() {
        if (preAnalisis.equals("opmult")) {
            //termino’→ opmult
            emparejar("opmult");
            factor();
            termino_prima(); //recursividad
        } else {

        }
    }
//—-----------------------------------------------------

//———————————————————————————————————————————————————————————————
//factor→ id  factor’ | num | num.num  |  ( expresion ) 
// Primeros factor = { id } ∪ { num } ∪ { num.num } ∪ { ( }
    private void factor() {
        if (preAnalisis.equals("id")) {
            //factor→ id  factor’ 
            emparejar("id");
            factor_prima();
        } else if (preAnalisis.equals("num")) {
            // factor -> num
            emparejar("num");
        } else if (preAnalisis.equals("num.num")) {
            //factor-> num.num
            emparejar("num.num");
        } else if (preAnalisis.equals("(")) {
            emparejar("(");
            expresion();
            emparejar(")");
        } else {
            error("[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    //PROCEDURE FACTOR'
    // PRIMERO factor’  = { ( } U  { empty }
    //Implementado por: Leonardo Zavala (21130874)
    private void factor_prima() {
        if (preAnalisis.equals("(")) {
            // factor' -> ( lista_expreisones )
            emparejar("(");
            lista_expresiones();
            emparejar(")");

        } else {
            // factor' -> empty
        }
    }
}
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
//::
