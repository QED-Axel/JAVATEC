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
 *: 29/Sep/2024 M.Mijares/A.Cabrera -Se corrigio el funcionamiento con retroceso.
 *:                                  ademas se agrego los procedure faltantes estos siendo:
 *:                                  metodo_principal, lista_proposiciones, proposicion_metodo
 *:                                  lista_expresiones, expresion, expresion', factor.
 *:
 *: 13/Nov/2024 M.Mijares AKA Soldado del amor/Cabrera AKA El mejor defensa 
 *:                                 -Se comenzo la implementacion de las acciones semanticas
 *:                                  del compilador JAVATec
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
    private boolean    analizarSemantica = false;
    private String     preAnalisis;
    //Variables para resolver problema de la gramatica
    private boolean retroceso; //Bandera que indica si se realizo un retorceso en el buffer de entrada
    private int ptr; //Apuntador auxiliar a una localidad del buffer
    
    
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
        clase(new Atributos());
        
    }

    //--------------------------------------------------------------------------

    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;            
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
 
    private void errorEmparejar(String _token, String _lexema, int numLinea ) {
        String msjError = "[emparejar] ";

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
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + 
                    ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico

    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    // Fin de error
    
    //Metodo para retroceder el simbolo de PreAnalisis en el Buffer de entrada
    //a la posicion previamente guardada en ptr
    
    private void retroceso (){
        cmp.be.setPrt(ptr);
        preAnalisis = cmp.be.preAnalisis.complex;
        retroceso = true;
    }
    
    
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------
    
    //----------- JAVATec Parte de Pegazon ----------------------------
    private void clase(Atributos clase/*Se agrega atributos a clase*/) {
        if (preAnalisis.equals("public")) {
            //Variables locales para los terminales y no terminales
            Linea_BE id = new Linea_BE();
            Atributos DE = new Atributos();
            Atributos DEM = new Atributos();
            Atributos MP = new Atributos();
            /* clase → public class  id  {   declaraciones    declaraciones_metodos    metodo_principal }*/
            emparejar("public");
            emparejar("class");
            id = cmp.be.preAnalisis; //Salvamos los atributos de id
            //Accion Semantica 1
            if( analizarSemantica ){
                cmp.ts.anadeTipo(id.entrada, "class");
            }
            //Fin accion Semantica 1
            emparejar("id");
            emparejar("{");
            declaraciones(DE);
            declaraciones_metodos(DEM);
            metodo_principal(MP);
            emparejar("}");
            //Accion Semantica 2
            if( analizarSemantica ){
                if( DE.tipo.equals(VACIO) && DEM.tipo.equals(VACIO) && MP.tipo.equals(VACIO)){
                    clase.tipo = VACIO;
                }else{
                    clase.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[clase] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 2
        } else {
            error("[clase] Error al iniciar la clase " + cmp.be.preAnalisis.numLinea);
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

    private void tipo_estandar(Atributos TE) {
        //Variables Locales
        Linea_BE INTE = new Linea_BE();
        Linea_BE FLOA = new Linea_BE();
        Linea_BE STRI = new Linea_BE();
        if (preAnalisis.equals("int")) {
            // tipo_estandar -> int
            INTE = cmp.be.preAnalisis;//Guardar el valor del integer
            emparejar("int");
            //Accion Semantica 7
            if ( analizarSemantica ){
                //TE.tipo = cmp.ts.buscaTipo(INTE.entrada);
                TE.tipo = "int";
            }
            //Fin accion Semantica 7
        } else if (preAnalisis.equals("float")) {
            // tipo_estandar -> float
            FLOA = cmp.be.preAnalisis;//Guardar el valor del float
            emparejar("float");
            //Accion Semantica 8
            if ( analizarSemantica ){
                //TE.tipo = cmp.ts.buscaTipo(FLOA.entrada);
                TE.tipo = "float";
            }
            //Fin accion Semantica 8
        } else if (preAnalisis.equals("string")) {
            // tipo_estandar -> string
            STRI = cmp.be.preAnalisis;
            emparejar("string");
            //Accion Semantica 9
            if ( analizarSemantica ){
                //TE.tipo = cmp.ts.buscaTipo(STRI.entrada);
                TE.tipo = "string";
            }
            //Fin accion Semantica 9
        } else {
            error("[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato estándar. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void dimension(Atributos dimension) {
        //Variables locales
        Linea_BE NUME = new Linea_BE();
        if (preAnalisis.equals("[")) {
            // dimension -> [ num ]
            emparejar("[");
            NUME = cmp.be.preAnalisis;
            emparejar("num");
            emparejar("]");
            //Accion Semantica 10
            if ( analizarSemantica ){
                dimension.longitud = NUME.lexema;
                dimension.esArreglo = true;
            }
            //Fin Accion Semantica 10
        } else {
            // dimension -> empty
            //Accion Semantica 11
            if ( analizarSemantica ){
                dimension.longitud = null;
                dimension.esArreglo = false;
            }
            //Fin accion Semantica 11
        }
    }

    private void declaraciones_metodos(Atributos DEM) {
        //Variables locales
        Atributos DEM1 = new Atributos();
        Atributos DM = new Atributos();
        retroceso = false;
        if (preAnalisis.equals("public")) {
            ptr = cmp.be.getptr();
            declaracion_metodo(DM);
            //declaraciones_metodos();
            if ( ! retroceso ){
                declaraciones_metodos(DEM1);
            }
            //Accion Semantica 15
            if ( analizarSemantica ){
                if ( DM.tipo.equals(VACIO) && DEM1.tipo.equals(VACIO)){
                    DEM.tipo = VACIO;
                }else{
                    DEM.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[declaraciones metodos] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 15
        } else {
            //empty
            //Accion Semantica 16
            if ( analizarSemantica ){
                DEM.tipo = VACIO;
            }
            //Fin accion Semantica 16
        }
    }

    private void declaracion_metodo(Atributos DM) {
        //Variables Locales 
        Atributos EM = new Atributos();
        Atributos PC = new Atributos();
        if (preAnalisis.equals("public") || preAnalisis.equals("{")) {
            encab_metodo(EM);
            //declaracion();
            //proposicion_compuesta();
            if ( ! retroceso ){
                proposicion_compuesta(PC);
            }
            //Accion Semantica 17
            if ( analizarSemantica ){
                if ( EM.tipo.equals(VACIO) && PC.tipo.equals(VACIO)){
                    DM.tipo = VACIO;
                }else{
                    DM.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion metodo] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 17
        } else {
            error("[declaracion_metodo] El programa debe iniciar con declaracion de variable con la palabra reservada(public static)");
        }

    }

    private void encab_metodo(Atributos EM) {
        if (preAnalisis.equals("public")) {
            // encab_metodo	→  public static  tipo_metodo  id( lista_parametros ) 
            emparejar("public");
            emparejar("static");
            tipo_metodo();
            if(! retroceso ){
                emparejar("id");
                emparejar("(");
                lista_parametros();
                emparejar(")");
            }
            /*emparejar("id");
            emparejar("(");
            lista_parametros();
            emparejar(")");*/
        } else {
            error("encab_metodo - Error de sintaxis "
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    public void metodo_principal(Atributos MP) {
        if (preAnalisis.equals("public")) {
            //metodo_principal();
            //metodo_principla();
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
            //empty
            error("[metodo_principal] El metodo no comienza con la palabra public"
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void tipo_metodo() {
        //Variable Local
        Atributos TE = new Atributos();
        if (preAnalisis.equals("void")) {
            // tipo_metodo -> void
            emparejar("void");
            if(preAnalisis.equals("main")){
                retroceso();
            }
        } else if (preAnalisis.equals("int")
                || preAnalisis.equals("float")
                || preAnalisis.equals("string")) {
            // tipo_metodo -> tipo_estandar corchetes
            tipo_estandar(TE);
            corchetes();
        } else {
            error("[tipo_metodo] - Tipo de dato incorrecto ( int, float, string ). "
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void corchetes() {
        if (preAnalisis.equals("[")) {
            // corchetes -> []
            emparejar("[");
            emparejar("]");
        } else {
            // corchetes -> empty
        }
    }

    private void lista_parametros() {
        //Variables locales
        Atributos tipo = new Atributos();
        Atributos dimension = new Atributos();
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            //lista_parametros →  tipo  id  dimension  lista_parametros’  | empty
            tipo(tipo);
            emparejar("id");
            dimension(dimension);
            lista_parametros_prima();
        } else {
            // lista_parametros -> empty
        }
    }

//FALTA CHEKO
    private void lista_parametros_prima() {
        //Variables locales
        Atributos tipo = new Atributos();
        Atributos dimension = new Atributos();
        if (preAnalisis.equals(",")) {
            //lista_parametros →  , tipo  id  dimension  lista_parametros’  | ϵ
            emparejar(",");
            tipo(tipo);
            emparejar("id");
            dimension(dimension);
            lista_parametros_prima();
        } else {
            // lista_parametros -> empty
        }
    }

    private void proposicion_compuesta(Atributos PC) {
        //Variables Locales
        Atributos PO = new Atributos();
        if (preAnalisis.equals("{")) {
            emparejar("{");
            proposiciones_optativas(PO);
            //Accion Semantica 18
            if ( analizarSemantica ){
                PC.tipo = PO.tipo;
            }
            //Fin accion Semantica 18 
            emparejar("}");
        } else {
            error("[proposicion_compuesta] Se esperaba (id): " + " - Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void proposiciones_optativas(Atributos PO) {
        //Variables Locales
        Atributos LP = new Atributos();
        if (preAnalisis.equals("id") || preAnalisis.equals("if") || preAnalisis.equals("while") || preAnalisis.equals("{")) {
            lista_proposiciones(LP);
            //Accion Semantica 19
            if ( analizarSemantica ){
                PO.tipo = LP.tipo;
            }
            //Fin accion Semantica 19
        } else {
            //Proposicion_optatica ->empty
            //Accion Semantica 20
            if ( analizarSemantica ){
                PO.tipo = VACIO;
            }
            //Fin accion Semantica 20
        }

    }

//FALTA Lista_proposiciones
    public void lista_proposiciones(Atributos LP) {
        //Variables Locales
        Atributos PROPO = new Atributos();
        Atributos LP1 = new Atributos();
        if (preAnalisis.equals("id") || preAnalisis.equals("{") || preAnalisis.equals("if") || preAnalisis.equals("while")) {
            proposicion(PROPO);
            lista_proposiciones(LP1);
            //Accion Semantica 21
            if ( analizarSemantica ){
                if( PROPO.tipo.equals(VACIO) && LP1.tipo.equals(VACIO) ){
                    LP.tipo = VACIO;
                }else{
                    LP.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista proposiciones] Hay incompatibilidad en el programa  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 21
        } else {
            //lista_proposiciones -> empty
            //Accion Semantica 22
            if ( analizarSemantica ){
                LP.tipo = VACIO;
            }
            //Fin accion Semantica 22
        }
    }

    public void proposicion(Atributos PROPO) {
        //Variables locales
        Linea_BE id = new Linea_BE();
        Atributos PP = new Atributos();
        Atributos PC = new Atributos();
        if (preAnalisis.equals("id")) {
            // proposicion -> id proposicion' ; 
            id = cmp.be.preAnalisis;//Guardamos el valor de id
            emparejar("id");
            //Accion Semantica 23
            if ( analizarSemantica ){
                if ( cmp.ts.buscaTipo(id.entrada) == null ){
                    cmp.ts.anadeTipo(id.entrada, PROPO.h);
                    PROPO.h = VACIO;
                }else{
                    PROPO.h = ERROR_TIPO;
                }
            }
            //Fin accion Semantica 23
            proposicion_prima(PP);
            //Accion Semantica 24
            if ( analizarSemantica ){
                if ( PROPO.h.equals(VACIO) && PP.h.equals(VACIO)){
                    PROPO.tipo = VACIO;
                }else{
                    PROPO.tipo = ERROR_TIPO;
                }
            }
            //Fin accion Semantica 24
            emparejar(";");
        } else if (preAnalisis.equals("{")) {
            // proposicion -> proposicion_compuesta
            proposicion_compuesta(PC);
            //Accion Semantica 25 
            if ( analizarSemantica ){
                PROPO.tipo = PC.tipo;
            }
            //Fin accion Semantica 25
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

    private void proposicion_prima(Atributos PP) {
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

//FALTA Proposicion metodo
    private void proposicion_metodo() {
        if (preAnalisis.equals("(")) {
            //proposicion metodo -> (lista_expresiones)
            emparejar("(");
            lista_expresiones();
            emparejar(")");
        } else {
            //proposicion_metodo -> empty
        }
    }

//FALTA Lista_Expresiones
    private void lista_expresiones() {

        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(") || preAnalisis.equals("literal")) {
            expresion();
            lista_expresiones_prima();
        } else {
            //lista_expresiones -> empty
        }

    }

    private void lista_expresiones_prima() {
        if (preAnalisis.equals(",")) {
            // lista_expresiones' -> , expresion lista_expresiones'
            emparejar(",");
            expresion();
            lista_expresiones_prima();
        } else {
            /*lista_expresiones' -> empty*/ }
    }

//FALTA Expresion
    private void expresion() {

        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {
            expresion_simple();
            expresion_prima();
        } else if (preAnalisis.equals("literal")) {
            emparejar("literal");
        } else {
            error("[expresion] Error en el valor inicial de expresion:" + cmp.be.preAnalisis.numLinea);
        }

    }

//FALTA Expresion_prima
    private void expresion_prima() {

        if (preAnalisis.equals("oprel")) {
            emparejar("oprel");
            expresion_simple();
        } else {
            //expresion_prima -> empty
        }

    }

    private void expresion_simple() {
        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {

            termino();
            expresion_simple_prima();

        } else {
            error("[expresion_simple]Tiene que empezar con (id, num, num.num, ( ). Linea: " + cmp.be.preAnalisis.numLinea);
        }

    }

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

    public void termino_prima() {
        if (preAnalisis.equals("opmult")) {
            //termino’→ opmult
            emparejar("opmult");
            factor();
            termino_prima(); //recursividad
        } else {
            //termino' -> empty
        }
    }

//FALTA Factor
    public void factor() {

        if (preAnalisis.equals("id")) {
            emparejar("id");
            factor_prima();
        } else if (preAnalisis.equals("num")) {
            emparejar("num");
        } else if (preAnalisis.equals("num.num")) {
            emparejar("num.num");
        } else if (preAnalisis.equals("(")) {
            emparejar("(");
            expresion();
            emparejar(")");
        } else {
            error("[factor] Se esperaba un factor (id, num, num.num, etc)" + "Linea:"
                    + cmp.be.preAnalisis.numLinea);
        }

    }

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
//::