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
 *:
 *: 14/nov/2024 SLopez & AVazquez -Se agregaron las acciones semanticas 2-25 ademas de la clase Atributos
 *:
 *: 18/nov/2024 LBarranco         -Se agregaron y corrigieron las acciones semanticas restantes
 *:
 *: 21/nov/2024 LBarranco          -Se corrigieron errores relacionados a metodo principal
 *:-----------------------------------------------------------------------------
 */
package compilador;

import general.Linea_BE;
import javax.swing.JOptionPane;

public class SintacticoSemantico {
    
    
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
        Atributos clase = new Atributos();
        clase(clase);

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
    private void clase(Atributos clase) {
        
        Atributos declaraciones = new Atributos();
        Atributos declaraciones_metodos = new Atributos();
        Atributos metodo_principal = new Atributos();
        Linea_BE id = new Linea_BE();

        
        if (preAnalisis.equals("public")) {
            /* clase → public class  id  {   declaraciones
declaraciones_metodos    metodo_principal }*/
            
            /* semantico
            clase 			→ public class  id {1} {
	     declaraciones
	     declaraciones_metodos
	      metodo_principal
	      } {2}
            */
            emparejar("public");
            emparejar("class");
            id = cmp.be.preAnalisis;
            /* accion semantica 1*/
            if(analizarSemantica){
                cmp.ts.anadeTipo(id.entrada, "class");
            }
            /* fin accion semantica 1*/
            emparejar("id");

            emparejar("{");
            declaraciones(declaraciones);
            declaraciones_metodos(declaraciones_metodos);
            metodo_principal(metodo_principal);
            emparejar("}");
            /* accion semantica 2:
            Clase.tipo := if ( declaraciones.tipo == VACIO AND 
            declaraciones_metodos.tipo == VACIO AND metodo_principal.tipo == VACIO) 
            then VACIO Else ERROR_TIPO*/
            if(analizarSemantica){
                if(declaraciones.tipo.equals(VACIO) && declaraciones_metodos.tipo.equals(VACIO) && metodo_principal.tipo.equals(VACIO)){
                    clase.tipo = VACIO;
                }else{
                    clase.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[clase] error en el tipo de la clase. D:"
                            +declaraciones.tipo+", DM: "+declaraciones_metodos.tipo+", MP: "+metodo_principal.tipo);
                }
            }
            /* fin accion semantica 2 */
        } else {
            error("[clase] Error al iniciar la clase se encontro (" + preAnalisis + ") se esperaba (public). Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }
///////////////////////////////////////////////////

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
                if ( cmp.ts.buscaTipo(id.entrada).equals("") ){
                    if ( dimension.esArreglo == true ){
                        cmp.ts.anadeTipo(id.entrada, "array(0.." + (dimension.longitud-1) + "," + LI.h + ")");
                    }else{
                        cmp.ts.anadeTipo(id.entrada, LI.h);
                    }
                    LI.tipo = VACIO;
                }else{
                    LI.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_identificadores] error al buscar el id. Linea:  " + cmp.be.preAnalisis.numLinea);
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
                if ( LIP.tipo.equals(VACIO) && LI.tipo.equals(VACIO)){
                    LI.tipo = VACIO;
                }else{
                    LI.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_identificadores 2] error en el tipo de LI' y LI. linea:  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 14
        } else {
            error("[lista_identificadores] - " //CAMBIAR EL MENSAJE
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    ///////////////////////////////////////////////////////////
    private void lista_identificadores_prima(Atributos LIP) {
        //Variables Locales
        Linea_BE id = new Linea_BE();
        Atributos dimension = new Atributos();
        Atributos LIP1 = new Atributos();
        
        if (preAnalisis.equals(",")) {

            // lista_identificadores’ -> , id  dimension  lista_identificadores’
            emparejar(",");
            id=cmp.be.preAnalisis;
            emparejar("id");
            dimension(dimension);
            
            /*inicia accion semantica 53*/
            if(analizarSemantica){
                if(cmp.ts.buscaTipo(id.entrada).equals("")){
                    if(dimension.esArreglo==true){
                        cmp.ts.anadeTipo(id.entrada,"array(0..."+(dimension.longitud-1)+","+LIP.h+")");
                        
                    }else{
                        cmp.ts.anadeTipo(id.entrada,LIP.h);
                    }
                    LIP.tipo = VACIO;
                }else{
                    LIP.tipo=ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_identificadores_prima] ya existe el id"
                            +id.lexema+". Linea:  " + cmp.be.preAnalisis.numLinea);

                }
            }
            /* fin accion 53 */
            
            /*inicia accion semantica 54*/
            if(analizarSemantica){
                LIP1.h = LIP.h;
            }
            /* fin accion 54 */
            
            lista_identificadores_prima(LIP1);
            
            /*inicia accion semantica 55*/
            if(analizarSemantica){
                if(LIP1.tipo.equals(VACIO) && LIP.tipo.equals(VACIO)){
                    LIP.tipo = VACIO;
                }else{
                    LIP.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_identificadores_prima] error en LI'1"
                            +LIP1.tipo+", LI': "+LIP.tipo+".Linea  " + cmp.be.preAnalisis.numLinea);

                }
            
            }
            /* fin accion 55 */
            

        } else {
            /*inicia accion semantica 56*/
            if(analizarSemantica){
                LIP.tipo=VACIO;
            }
            /* fin accion 56 */
            
            // lista_identificadores’ -> empty 
        }
    }

    ////////////////////////////////////////////////////////////////////////
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
        
            
            
            
            if ( ! retroceso ){
                
                //Accion Semantica 3
                if ( analizarSemantica ){
                    LI.h = tipo.tipo;
                }
                //Fin accion Semantica 3
                

//                System.out.println(preAnalisis);
//                cmp.be.siguiente();
//                    
//                System.out.println(preAnalisis);
//                if(preAnalisis.equals(";")){
//                    cmp.be.anterior();
//                    System.out.println(preAnalisis);
//                }else{
//                    retroceso();
//                }
                emparejar("id");
//                System.out.println(preAnalisis);
                if ( preAnalisis.equals( ";" ) || preAnalisis.equals(",")|| preAnalisis.equals("[")){
                    //Si es punto y coma se trata de una sentencia de declaracion de variables
                    cmp.be.setPrt(ptr+3);
                    preAnalisis = cmp.be.preAnalisis.complex;
                    lista_identificadores(LI);
                    emparejar(";");
                    declaraciones(DE1);
            
                    //Accion Semantica 4
                    if ( analizarSemantica ){
                        if ( LI.tipo.equals(VACIO) && DE1.tipo.equals(VACIO)){
                    
                            DE.tipo = VACIO;
                
                        }else{
                    
                            DE.tipo = ERROR_TIPO;
                    
                            cmp.me.error(Compilador.ERR_SEMANTICO, "[declaraciones] error. LI:"
                                    +LI.tipo+", DE1:  "+DE1.tipo+". Linea:" + cmp.be.preAnalisis.numLinea);
                        }
                    }
                    //Fin accion Semantica 4
                    
                }else{
                    retroceso();
                }
                
            }
            if(retroceso==true){  
                //Accion Semantica 5 
                // ELIMINAR EL ULTIMO AGREGADO A LA TABLA DE SIMBOLOS
                if ( analizarSemantica ){
//                    System.out.println(cmp.ts.getTamaño());
//                    cmp.ts.anadeTipo(cmp.ts.getTamaño()-1, "");
                    DE.tipo = VACIO;
                }
                //Fin accion Semantica 5
            }
        } else {
            //declaraciones -> ϵ
            //Accion Semantica 5 
            if ( analizarSemantica ){
                DE.tipo = VACIO;
            }
            //Fin accion Semantica 5
        }
    }

    ////////////////////////////////////////////////////////////////////////
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

    
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void tipo_estandar(Atributos TE) {
        //Variables Locales
        Linea_BE INTE = new Linea_BE();
        Linea_BE FLOA = new Linea_BE();
        Linea_BE STRI = new Linea_BE();
        
        
        if (preAnalisis.equals("int")) {
            // tipo_estandar -> int
//            INTE = cmp.be.preAnalisis;//Guardar el valor del integer
            emparejar("int");
            
            
            
            //Accion Semantica 7
            if ( analizarSemantica ){
                //TE.tipo = cmp.ts.buscaTipo(INTE.entrada);
                TE.tipo = "int";
            }
            //Fin accion Semantica 7
            
            
            
        } else if (preAnalisis.equals("float")) {
            // tipo_estandar -> float
//            FLOA = cmp.be.preAnalisis;//Guardar el valor del float
            emparejar("float");
            
            
            //Accion Semantica 8
            if ( analizarSemantica ){
                //TE.tipo = cmp.ts.buscaTipo(FLOA.entrada);
                TE.tipo = "float";
            }
            //Fin accion Semantica 8
            
            
        } else if (preAnalisis.equals("string")) {
            // tipo_estandar -> string
//            STRI = cmp.be.preAnalisis;
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

    
    ////////////////////////////////////////////////////////////////////////
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
                dimension.longitud = Integer.parseInt(NUME.lexema);
                dimension.esArreglo = true;
            }
            //Fin Accion Semantica 10
            
            
            
        } else {
            // dimension -> empty
            
            
            //Accion Semantica 11
            if ( analizarSemantica ){
                dimension.longitud = 0;
                dimension.esArreglo = false;
            }
            //Fin accion Semantica 11
            
            
        }
    }

    ////////////////////////////////////////////////////////////////////////
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
                
                //Accion Semantica 15
                if ( analizarSemantica ){
                    if ( DM.tipo.equals(VACIO) && DEM1.tipo.equals(VACIO)){
                        DEM.tipo = VACIO;
                    }else{
                        DEM.tipo = ERROR_TIPO;
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[declaraciones metodos] error en DM:"
                                +DM.tipo+", DEM1: "+DEM1.tipo+"  " + cmp.be.preAnalisis.numLinea);
                    }
                }
                //Fin accion Semantica 15
            }
            
            if(retroceso==true){  
                //Accion Semantica 16
                if ( analizarSemantica ){
                    DEM.tipo = VACIO;
                }
                //Fin accion Semantica 16
            }
            
            
            
            
        } else {
            //empty
            
            
            
            //Accion Semantica 16
            if ( analizarSemantica ){
                DEM.tipo = VACIO;
            }
            //Fin accion Semantica 16
            
            
            
        }
    }

    ////////////////////////////////////////////////////////////////////////    
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
                
                //Accion Semantica 17
                if ( analizarSemantica ){
                    if ( EM.tipo.equals(VACIO) && PC.tipo.equals(VACIO)){
                        DM.tipo = VACIO;
                    }else{
                        DM.tipo = ERROR_TIPO;
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion metodo] error en EM:"
                                +EM.tipo+" o PC"+PC.tipo+". Linea:  " + cmp.be.preAnalisis.numLinea);
                    }
                }
                //Fin accion Semantica 17
            }
            
            
            
            
            
        } else {
            error("[declaracion_metodo] El programa debe iniciar con declaracion de variable con la palabra reservada(public static)");
        }

    }

//—--------------------------------------------------------------------------
// Implementado por: Alejandro Huerta Reyna 21130857
    private void encab_metodo(Atributos encab_metodo) {
        
        Atributos tipo_metodo = new Atributos();
        Atributos lista_parametros = new Atributos();
        Linea_BE id = new Linea_BE();
        
        if (preAnalisis.equals("public")) {
            // encab_metodo →  public static  tipo_metodo  id( lista_parametros )
            emparejar("public");
            emparejar("static");
            tipo_metodo(tipo_metodo);
            if (preAnalisis.equals("id")) {
                id=cmp.be.preAnalisis;
                emparejar("id");
                emparejar("(");
                lista_parametros(lista_parametros);
                emparejar(")");
                
                /* accion semantica 57 */
                if(analizarSemantica){
                    if(cmp.ts.buscaTipo(id.entrada).equals("")){
                        cmp.ts.anadeTipo(id.entrada, lista_parametros.h+"->"+tipo_metodo.tipo);
                        encab_metodo.tipo = VACIO;
                    }else{
                        encab_metodo.tipo = ERROR_TIPO;
            
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[encab_metodo] error en buscaTipo:"
                                +cmp.ts.buscaTipo(id.entrada)+". Linea:  " + cmp.be.preAnalisis.numLinea);
                    
                    }
                }
                /* fin accion semantica 57*/
                
            } else if (preAnalisis.equals("main")) {
                retroceso();
            }else{
                error("[encab_metodo] - Error de sintaxis "
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
            
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
    private void metodo_principal(Atributos metodo_principal) {
        
        Atributos proposicion_compuesta = new Atributos();
        Linea_BE main = new Linea_BE();
        
        if (preAnalisis.equals("public")) {
            //metodo_principal → public static void  main ( string  args [ ]  )   proposición_compuesta
            emparejar("public");
            emparejar("static");
            emparejar("void");
            main = cmp.be.preAnalisis;
            emparejar("main");
            emparejar("(");
            emparejar("string");
            emparejar("args");
            emparejar("[");
            emparejar("]");
            emparejar(")");
            proposicion_compuesta(proposicion_compuesta);
            
            /* accion semantic 66 */
            if(analizarSemantica){
                if(cmp.ts.buscaTipo(main.entrada).equals("")){
                    cmp.ts.anadeTipo(main.entrada, "string -> void");
                    if(proposicion_compuesta.tipo.equals(VACIO)){
                        metodo_principal.tipo=VACIO;
                    }else{
                        metodo_principal.tipo=ERROR_TIPO;

                        cmp.me.error(Compilador.ERR_SEMANTICO, "[metodo principal] error en proposicion compuesta: " + cmp.be.preAnalisis.numLinea);

                    }
                }else{
//                    System.out.println(cmp.ts.buscaTipo(main.entrada));
                    metodo_principal.tipo=ERROR_TIPO;

                    cmp.me.error(Compilador.ERR_SEMANTICO, "[metodo principal] error al buscar main : " + cmp.be.preAnalisis.numLinea);
                }
//                if(proposicion_compuesta.tipo.equals(VACIO)){
//                        metodo_principal.tipo=VACIO;
//                }else{
//                        metodo_principal.tipo=ERROR_TIPO;
//                        cmp.me.error(Compilador.ERR_SEMANTICO, "[metodo principal] error en proposicion compuesta: " + cmp.be.preAnalisis.numLinea);
//                }
            }
            /* fin accion 66 */
            
        } else {
            error("[metodo_principal] se encontro (" + preAnalisis + ") se esperaba (public), Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }
//—-----------------------------------------------------------------------------

//------------------------------------------------------------------------------
// CODIGO tipo_metodo
// PRIMERO ( tipo_metodo )   = { ‘void’ , ‘int’ , ‘float’ , ‘string’ }
// Implementado por: Marcos Juárez ( 21130852 )
    private void tipo_metodo(Atributos tipo_metodo) {
        
        Atributos tipo_estandar = new Atributos();
        Atributos corchetes = new Atributos();
        
        if (preAnalisis.equals("void")) {
            // tipo_metodo -> void
            emparejar("void");
            

            /* accion semantica 58 */
            if(analizarSemantica){
                tipo_metodo.tipo = "void";
            }
            /* fin accion 58 */


        } else if (preAnalisis.equals("int")
                || preAnalisis.equals("float")
                || preAnalisis.equals("string")) {
            // tipo_metodo -> tipo_estandar corchetes
            tipo_estandar(tipo_estandar);
            corchetes(corchetes);
            
            /* accion semantica 59 */
            if(analizarSemantica){
                if( corchetes.tipo.equals(VACIO)){
                    tipo_metodo.tipo = tipo_estandar.tipo;
                }else{
                    tipo_metodo.tipo = tipo_estandar.tipo+corchetes.tipo;
                }
            }
            /* fin accion 59 */
            
        } else {
            error("[tipo_metodo] - Tipo de dato incorrecto ( int, float, string ). "
                    + "Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------
//PRIMEROS(corchetes) = { [ } U { ϵ }
//Implementado por: Jesus Emmanuel Llamas Hernandez - 21130904
    private void corchetes(Atributos corchetes) {
        
        
        
        if (preAnalisis.equals("[")) {
            // corchetes -> []
            emparejar("[");
            emparejar("]");
            
            /* inicia accion semantica 60 */
            if(analizarSemantica){
                corchetes.tipo = "[]";
            }
            /* fin accion 60 */
            
        } else {
            
            
            /* inicia accion semantica 61 */
            if(analizarSemantica){
                corchetes.tipo = VACIO;
            }
            /* fin accion 61 */
            
            // corchetes -> ϵ
        }
    }

//-------------------------------------------------------------------------------
    //PROCEDURE lista_parametros
    //Primeros ( lista_parametros ) = { int, float, string } U { empty }
    //Implementado por: Rodrigo Macias Ruiz (21131531)
    private void lista_parametros(Atributos lista_parametros) {
        
        Atributos tipo = new Atributos();
        Linea_BE id = new Linea_BE();
        Atributos dimension = new Atributos();
        Atributos lista_parametros_prima = new Atributos();
        
        if (preAnalisis.equals("int") || preAnalisis.equals("float") || preAnalisis.equals("string")) {
            //lista_parametros →  tipo  id  dimension  lista_parametros’  | ϵ
            tipo(tipo);
            id=cmp.be.preAnalisis;
            emparejar("id");
            dimension(dimension);
            lista_parametros_prima(lista_parametros_prima);
            
            
            /* inicia accion semantica 62 */
            if(analizarSemantica){
                if(cmp.ts.buscaTipo(id.entrada).equals("")){
                    if(dimension.esArreglo){
                        lista_parametros.h = lista_parametros.h+" X array(0.."+(dimension.longitud-1)+","+tipo.tipo+")";
                        cmp.ts.anadeTipo(id.entrada, "array(0.."+(dimension.longitud-1)+","+tipo.tipo+")");
                    }else{
                        lista_parametros.h = lista_parametros.h+""+tipo.tipo;
                        cmp.ts.anadeTipo(id.entrada, tipo.tipo);
                    }
                    lista_parametros.h = lista_parametros.h+lista_parametros_prima.h;
                    lista_parametros.tipo=VACIO;
                }else{
                    lista_parametros.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_parametros] error en buscaTipo:"
                            +cmp.ts.buscaTipo(id.entrada));
                }
            }
            /* fin accion 62 */
            
        } else {
            
            
            /* inicia accion semantica 63 */
            if(analizarSemantica){
                lista_parametros.tipo=VACIO;
            }
            /* fin accion 63 */
            
            // lista_parametros -> empty
        }
    }

//-------------------------------------------------------------------------------
    //PROCEDURE lista_parametros 
    //Primeros ( lista_parametros ) = { , } U { empty } 
    //Implementado por: Rodrigo Macias Ruiz (21131531)
    private void lista_parametros_prima(Atributos lista_parametros_prima) {
        Atributos tipo = new Atributos();
        Linea_BE id = new Linea_BE();
        Atributos dimension = new Atributos();
        Atributos lista_parametros_prima_1 = new Atributos();
        
        if (preAnalisis.equals(",")) {
            //lista_parametros →  , tipo  id  dimension  lista_parametros’  | ϵ
            emparejar(",");
            tipo(tipo);
            id=cmp.be.preAnalisis;
            emparejar("id");
            dimension(dimension);
            lista_parametros_prima(lista_parametros_prima);
            
            
            /* inicia accion semantica 64 */
            if(analizarSemantica){
                if(cmp.ts.buscaTipo(id.entrada).equals("")){
                    if(dimension.esArreglo){
                        lista_parametros_prima.h = lista_parametros_prima.h +" X array(0..."+(dimension.longitud-1)+","+tipo.tipo+")";
                        cmp.ts.anadeTipo(id.entrada, "array(0..."+(dimension.longitud-1)+","+tipo.tipo+")");
                    }else{
                        lista_parametros_prima.h = lista_parametros_prima.h+" X "+tipo.tipo;
                        cmp.ts.anadeTipo(id.entrada,tipo.tipo);
                    }
                    lista_parametros_prima.h = lista_parametros_prima.h+lista_parametros_prima_1.h;
                    lista_parametros_prima.tipo = VACIO;
                }else{
                    lista_parametros_prima.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_parametros_prima] error en buscaTipo:"
                            +cmp.ts.buscaTipo(id.entrada));
                }
            }
            /* fin accion 64 */
            
        } else {
            
            /* accion semantica 65 */
            if(analizarSemantica){
                lista_parametros_prima.tipo = VACIO;
            }
            /* fin accion 65*/
            
            // lista_parametros -> empty
        }
    }

    //-------------------------------------------------------------------

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

    
    ////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////
    private void lista_proposiciones(Atributos LP) {
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
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista proposiciones] error en PROPO:"
                            +PROPO.tipo+", L'1: "+LP1.tipo+".Lista:  " + cmp.be.preAnalisis.numLinea);
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

    ///////////////////////////////////////////////////////////////////////
    private void proposicion(Atributos PROPO) {
        //Variables locales
        Linea_BE id = new Linea_BE();
        Atributos PP = new Atributos();
        Atributos PC = new Atributos();
        Atributos expresion = new Atributos();
        Atributos proposicion1 = new Atributos();
        Atributos proposicion2 = new Atributos();
        
        
        
        
        if (preAnalisis.equals("id")) {
            // proposicion -> id proposicion' ; 
            id = cmp.be.preAnalisis;//Guardamos el valor de id
            emparejar("id");
            proposicion_prima(PP);
            emparejar(";");
            
            
            //Accion Semantica 23
            if ( analizarSemantica ){
                if ( cmp.ts.buscaTipo(id.entrada).equals(PP.h) || !cmp.ts.buscaTipo(id.entrada).equals("") ){
                    if(PP.tipo.equals(VACIO)){
//                        if(PP.esArreglo==true){
//                            cmp.ts.anadeTipo(id.entrada,"array(0..."+(PP.longitud-1)+","+PP.h+")");
//                        }else{
//                            cmp.ts.anadeTipo(id.entrada,PP.h);
//                        }
                        PROPO.tipo=VACIO;
                    }else{
                        PROPO.tipo=ERROR_TIPO;
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion] proposicion prima no es vacio  " + cmp.be.preAnalisis.numLinea);
                    }
                }else{
                    PROPO.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion] error buscaTipo "+
                            cmp.ts.buscaTipo(id.entrada)+". Linea: " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 23
            
            
            
        } else if (preAnalisis.equals("{")) {
            // proposicion -> proposicion_compuesta
            proposicion_compuesta(PC);
            
            //Accion Semantica 24
            if ( analizarSemantica ){
                PROPO.tipo = PC.tipo;
            }
            //Fin accion Semantica 24
            
            
            
            
        } else if (preAnalisis.equals("if")) {
            
            
            // proposicion -> if ( expresión ) proposición else proposición
            emparejar("if");
            emparejar("(");
            expresion(expresion);
            emparejar(")");
            proposicion(proposicion1);
            emparejar("else");
            proposicion(proposicion2);
            
            //Accion Semantica 25 
            if ( analizarSemantica ){
                if(expresion.tipo.equals(VACIO) && proposicion1.tipo.equals(VACIO) && proposicion2.tipo.equals(VACIO)){
                    PROPO.tipo = VACIO;
                }else{
                    PROPO.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion] error en expresion:"
                            +expresion.tipo+", proposicion1:"+proposicion1.tipo
                            +", proposicion2:"+proposicion2.tipo+". Linea  " + cmp.be.preAnalisis.numLinea);
                }
            }
            //Fin accion Semantica 25
            
            
        } else if (preAnalisis.equals("while")) {
            // proposicion -> while ( expresión ) proposición 
            emparejar("while");
            emparejar("(");
            expresion(expresion);
            emparejar(")");
            proposicion(proposicion1);
            
            
            /*accion 26
            Proposicion.tipo := if(expresion.tipo == VACIO AND proposicion1.tipo == VACIO) then VACIO else ERROR_TIPO
            */
            if(analizarSemantica){
                if(expresion.tipo.equals(VACIO) && proposicion1.tipo.equals(VACIO)){
                    PROPO.tipo=VACIO;
                }else{
                    PROPO.tipo=ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion] expresion "
                            +expresion.tipo+", proposicion1"+proposicion1.tipo+".Linea  " + cmp.be.preAnalisis.numLinea);
                }
            }
            /*fin accion 26*/
            
            
        } else {
            error("[proposicion] Error en la proposicion Linea:" + cmp.be.preAnalisis.numLinea);
        }

    }

    //////////////////////////////////////////////////////////////////////
    //Procedure Proposicion’
//
// PRIMERO (proposicion') = { [ } U { opasig } U { ( } U { empty };
//
// Diseñado por: Manuel Mijares Lara Hola Noloh :), noloh deja de usar el cel
    private void proposicion_prima(Atributos proposicion_prima) {
        Atributos expresion = new Atributos();
        Atributos expresion1 = new Atributos();
        Atributos proposicion_metodo = new Atributos();
        
        if (preAnalisis.equals("[")) {
            //proposicion' -> [expresion] opasig expresion
            emparejar("[");
            expresion(expresion);
            emparejar("]");
            emparejar("opasig");
            expresion(expresion1);
            /* inicio accion 27 
            If (expresion.tipo == “int” AND expresion1.tipo != ERROR_TIPO)then{ proposicion’.long := expresion.h
proposicion’.esArreglo := true
proposicion’.h := expresion1.h
proposicion’.tipo := expresion1.h }else{
proposicion’.tipo := ERROR_TIPO }*/
            
            if(analizarSemantica){
                if(expresion.tipo.equals("int") && (!expresion1.tipo.equals(ERROR_TIPO))){
                    proposicion_prima.longitud = Integer.parseInt(expresion.h);
                    proposicion_prima.esArreglo=true;
                    proposicion_prima.h=expresion1.h;
                    proposicion_prima.tipo = expresion1.h;
                }else{
                    proposicion_prima.tipo=ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion_prima] expresion no es int,"+
                            expresion.tipo+", y expresion1 es error_tipo:"+expresion1.tipo+".Linea  " + cmp.be.preAnalisis.numLinea);
                }
            }
            
            /* fin accion 27 */
            
            
            
        } else if (preAnalisis.equals("opasig")) {
            //proposicion' -> opasig expresion
            emparejar("opasig");
            expresion(expresion);
            
            /* inicia accion 28 
            If (expresion.tipo != ERROR_TIPO)then{ proposicion’.esArreglo := false proposicion’.h := expresion.h proposicion’.tipo := expresion.tipo
            }else{proposicion’.tipo := ERROR_TIPO}*/
            if(analizarSemantica){
                if(!expresion.tipo.equals(ERROR_TIPO)){
                    proposicion_prima.esArreglo = false;
//                    System.out.println("tipo de expresion.h:"+expresion.h);
                    proposicion_prima.h = expresion.h;
//                    System.out.println("tipo de expresion:"+expresion.tipo);
                    proposicion_prima.tipo = expresion.tipo;
                }else{
                    proposicion_prima.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion_prima] error expresion:"
                            +expresion.tipo+".Linea  " + cmp.be.preAnalisis.numLinea);
                }
            }
            /* fin accion 28*/
            
        } else if (preAnalisis.equals("(")) {
            //proposicion' -> proposicion_metodo
            proposicion_metodo(proposicion_metodo);
            
            /* inicia accion 29 */
            if(analizarSemantica){
                proposicion_prima.tipo = proposicion_metodo.tipo;
            }
            /* fin accion 29 */
            
        } else {
            
            /*inicia accion 30*/
            if(analizarSemantica){
                proposicion_prima.tipo = VACIO;
            }
            /*fin accion 30*/
            //proposicion' -> empty
        }
    }


//———————————————————————————————————————————————————————————————
//proposición_metodo→ ( lista_expresiones )  | ϵ
// Primeros proposición_metodo = { lista_expresiones } ∪ { empty }
    private void proposicion_metodo(Atributos proposicion_metodo) {
        
        Atributos lista_expresiones = new Atributos();
        
        if (preAnalisis.equals("(")) {
            //proposición_metodo = { lista_expresiones }
            emparejar("(");
            lista_expresiones(lista_expresiones);
            emparejar(")");
            
            /*inicia accion semantica 31
            Proposicion_metodo.tipo := lista_expresiones.tipo; proposicion_metodo.h := lista_expresiones.h;*/
            if(analizarSemantica){
                proposicion_metodo.tipo = lista_expresiones.tipo;
                proposicion_metodo.h = lista_expresiones.h;
            }
            /*fin accion 31*/
            
            
        } else {
            
            
            /*inicia accion semantica 32
            Proposicion_metodo.tipo := VACIO*/
            if(analizarSemantica){
                proposicion_metodo.tipo = VACIO;
            }
            /*fin accion 32*/
            

            //proposicion_metodo -> empty
        }

    }

//———————————————————————————————————————————————————————————————
//lista_expresiones → expresión    lista_expresiones’  | ϵ
// Primeros lista_expresiones = { expresión } ∪ { empty }
    private void lista_expresiones(Atributos lista_expresiones) {
        
        Atributos expresion = new Atributos();
        Atributos lista_expresiones_prima = new Atributos();
        
        
        /*accion 33
        if(lista_expresiones’.tipo == “float”) then{
            lista_expresiones.h=lista_expresiones’.h;
            lista_expresiones.tipo=lista_expresiones’.tipo; 
        }else{
            lista_expresiones.h=expresion.h;
            lista_expresiones.tipo=expresion.tipo; }*/
        
        if (preAnalisis.equals("id")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion(expresion);
            lista_expresiones_prima(lista_expresiones_prima);
            
            
            /* accion 33 */
            if(analizarSemantica){
                lista_expresiones.h=expresion.h;
                lista_expresiones.tipo=expresion.tipo;
            }
            /* fin accion 33 */
            
            
        } else if (preAnalisis.equals("num")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion(expresion);
            lista_expresiones_prima(lista_expresiones_prima);
            
            
            /* accion 33 */
            if(analizarSemantica){
                lista_expresiones.h=expresion.h;
                lista_expresiones.tipo=expresion.tipo;
            }
            /* fin accion 33 */
            
            
        } else if (preAnalisis.equals("num.num")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion(expresion);
            lista_expresiones_prima(lista_expresiones_prima);
            /*accion 33*/
            if(analizarSemantica){
                lista_expresiones.h=lista_expresiones_prima.h;
                lista_expresiones.tipo=lista_expresiones_prima.tipo;
            }
            /*fin accion 33*/
        } else if (preAnalisis.equals("(")) {
            //lista_expresiones → {expresión    lista_expresiones’}    
            expresion(expresion);
            lista_expresiones_prima(lista_expresiones_prima);
            
            /* accion 33 */
            if(analizarSemantica){
                lista_expresiones.h=expresion.h;
                lista_expresiones.tipo=expresion.tipo;
            }
            /* fin accion 33 */
            
            
        } else {
            
            /* inicia accion 34 
            lista_expresiones.tipo := VACIO*/
            if(analizarSemantica){
                lista_expresiones.tipo = VACIO;
            }
            /* fin accion 34 */
            
            // lista_expresiones -> empty    
        }
    }
    //--------------------------------------------------------------------------
    // PROCEDURE lista_expresiones'
    // PRIMERO(lista_expresiones') = { ',' } U { 'empty' }
    // Implementado por: Diego Muñoz Rede (21130893)

    private void lista_expresiones_prima(Atributos lista_expresiones_prima) {
        
        Atributos expresion = new Atributos();
        Atributos lista_expresiones_prima_1 = new Atributos();
        
        if (preAnalisis.equals(",")) {
            // lista_expresiones' -> , expresion lista_expresiones'
            emparejar(",");
            expresion(expresion);
            lista_expresiones_prima(lista_expresiones_prima_1);
            
            
            /* inicia accion semantica 35 
            if(lista_expresiones’1.tipo == “float”) then{
                lista_expresiones’.h=lista_expresiones’1.h;
                lista_expresiones’.tipo=lista_expresiones’1.tipo; 
            }else{
                lista_expresiones’.h=expresion.h;
                lista_expresiones’.tipo=expresion.tipo; }*/
            if(analizarSemantica){
                if(lista_expresiones_prima_1.tipo.equals("float")){
                    lista_expresiones_prima.h = lista_expresiones_prima_1.h;
                    lista_expresiones_prima.tipo = lista_expresiones_prima_1.tipo;
                }else{
                    lista_expresiones_prima.h=expresion.h;
                    lista_expresiones_prima.tipo = expresion.tipo;
                }
            }
            /* fin accion 35*/
            
            
            
        } else {
            /* inicia accion semantica 36 */
            if(analizarSemantica)
                lista_expresiones_prima.tipo = VACIO;
            /* fin accion semantica 36 */
            
            /*lista_expresiones' -> empty*/ }
    }

//———————————————————————————————————————————————————————————————
//expresión → expresión_simple  expresion’   |  literal
// Primeros expresion = { expresion_simple } ∪ { literal }
    private void expresion(Atributos expresion) {
        
        
        Atributos expresion_simple = new Atributos();
        Atributos expresion_prima = new Atributos();
        Linea_BE literal = new Linea_BE();
        
        
        if (preAnalisis.equals("id")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple(expresion_simple);
            expresion_prima(expresion_prima);
            
            
            /* accion semantica 37 
            expresion.h=expresion_simple.h;
            expresion.tipo=expresion_simple.tipo;*/
            if(analizarSemantica){
                expresion.h = expresion_simple.h;
                expresion.tipo = expresion_simple.tipo;
            }
            /* fin accion semantica 37*/
            
            
        } else if (preAnalisis.equals("num")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple(expresion_simple);
            expresion_prima(expresion_prima);
            
            /* accion semantica 37 
            expresion.h=expresion_simple.h;
            expresion.tipo=expresion_simple.tipo;*/
            if(analizarSemantica){
                expresion.h = expresion_simple.h;
                expresion.tipo = expresion_simple.tipo;
            }
            /* fin accion semantica 37*/
            
        } else if (preAnalisis.equals("num.num")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple(expresion_simple);
            expresion_prima(expresion_prima);
            
            /* accion semantica 37 
            expresion.h=expresion’.h;
            expresion.tipo=expresion’.tipo;*/
            if(analizarSemantica){
                expresion.h = expresion_prima.h;
                expresion.tipo = expresion_prima.tipo;
            }
            
            /* fin accion 37 */
            
        } else if (preAnalisis.equals("(")) {
            //expresion → {expresión_simple    expresion_prima}    
            expresion_simple(expresion_simple);
            expresion_prima(expresion_prima);
            
            
            /* accion semantica 37 
            expresion.h=expresion_simple.h;
            expresion.tipo=expresion_simple.tipo;*/
            if(analizarSemantica){
                expresion.h = expresion_simple.h;
                expresion.tipo = expresion_simple.tipo;
            }
            /* fin accion semantica 37*/
            
            
        } else if (preAnalisis.equals("literal")) {
            // expresion -> literal
            literal=cmp.be.preAnalisis;
            emparejar("literal");
            
            
            /* inicia accion 38 
            Expresion.tipo := VACIO;
            expresion.h := literal.lexema;*/
            if(analizarSemantica){
                expresion.tipo = VACIO;
                expresion.h = literal.lexema;
            }
            /* fin accion 38 */
            
        } else {
            error("[expresion] error se esperaba id,num,num.num,(,literal. "
                    + "Se encontro: (" + preAnalisis + ") - Linea: " 
                    + cmp.be.preAnalisis.numLinea);
        }

    }

//———————————————————————————————————————————————————————————————
//expresion’ → oprel   expresión_simple  |  ϵ
// Primeros expresion = { oprel } ∪ { empty }
    private void expresion_prima(Atributos expresion_prima) {
        
        Atributos expresion_simple = new Atributos();
        
        if (preAnalisis.equals("oprel")) {
            //expresion_prima → {oprel expresion_simple}
            emparejar("oprel");
            expresion_simple(expresion_simple);
            
            
            /* accion semantica 39 
            Expresion’.tipo := expresion_simple.tipo; expresion’.h := expresion_simple.h;*/
            if(analizarSemantica){
                expresion_prima.tipo = expresion_simple.tipo;
                expresion_prima.h = expresion_simple.h;
            }
            /* fin accion semantica*/
            
        } else {
            
            /*accion semantica 40
            Expresion’.tipo := VACIO*/
            if(analizarSemantica){
                expresion_prima.tipo = VACIO;
            }
            /*fin accion semantica 40*/
            
        // expresion’ -> empty
        }
    }

    //-------------------------------------------------------------------
//PROCEDURE expresion_simple
//Primero (expresion_simple) = PRIMEROS(termino) = {id,num, num.num,(}
//Implementado por: Miriam Alicia Sanchez Cervantes (21130882)
    private void expresion_simple(Atributos expresion_simple) {
        
        Atributos termino = new Atributos();
        Atributos expresion_simple_prima  = new Atributos();
        
        if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {
            termino(termino);
            expresion_simple_prima(expresion_simple_prima);
            
            /*accion semantica 41
            if(expresion_simple’.tipo == “float”) then{
                expresion_simple.h=expresion_simple’.h;
                expresion_simple.tipo=expresion_simple’.tipo; 
            }else{
                expresion_simple.h=termino.h;
                expresion_simple.tipo=termino.tipo; }*/
            
            if(expresion_simple_prima.tipo.equals("float")){
                expresion_simple.h=expresion_simple_prima.h;
                expresion_simple.tipo = expresion_simple_prima.tipo;
            }else{
                expresion_simple.h=termino.h;
                expresion_simple.tipo=termino.tipo;
            }
            /*fin accion semantica 41*/
            
        } else {
            error("[expresion_simple]Tiene que empezar con (id, num, num.num, ( ). Linea: " + cmp.be.preAnalisis.numLinea);
        }

    }

//-----------------------------------------------------------------------------
// Código implementado en Java
// PRIMEROS (expresion_simple_prima) = {'opsuma' } U { 'empty' }
// Implementado por : María Fernanda Torres Herrera (21130859)
    private void expresion_simple_prima(Atributos expresion_simple_prima) {
        
        Atributos termino = new Atributos();
        Atributos expresion_simple_prima_1 = new Atributos();
        
        
        if (preAnalisis.equals("opsuma")) {
            // expresion_simple_prima -> opsuma termino expresion_simple_prima
            emparejar("opsuma");
            termino(termino);
            expresion_simple_prima(expresion_simple_prima_1);
            
            /* accion semantica 42 
            if(expresion_simple’1.tipo == “float”) then{
                expresion_simple’.h=expresion_simple’1.h;
                expresion_simple’.tipo=expresion_simple’1.tipo; 
            }else{
                expresion_simple’.h=termino.h;
                expresion_simple’.tipo=termino.tipo; }*/
            if(analizarSemantica){
                if(expresion_simple_prima_1.tipo.equals("float")){
                    expresion_simple_prima.h=expresion_simple_prima_1.h;
                    expresion_simple_prima.tipo=expresion_simple_prima_1.tipo;
                }else{
                    expresion_simple_prima.h=termino.h;
                    expresion_simple_prima.tipo = termino.tipo;
                }
            }
            /*fin accion semantica*/


        } else {
            
            /*accion semantica 43
            expresion_simple’.tipo := VACIO*/
            if(analizarSemantica){
                expresion_simple_prima.tipo = VACIO;
            }
            /*fin accion semantica 43*/
            
            // expresion_simple_prima -> empty
        }
    }

    //-------------------------------------------------------------------------------
// PROCEDURE termino
// PRIMEROS ( termino )  = PRIMEROS ( factor ) 
//                       = { id, num, num.num, ( }
// Implementando por:  Juan Fernando Vaquera Sanchez 21130869
    private void termino(Atributos termino) {
        
        Atributos factor = new Atributos();
        Atributos termino_prima = new Atributos();

        if (preAnalisis.equals("id") || preAnalisis.equals("num")
                || preAnalisis.equals("num.num") || preAnalisis.equals("(")) {

            // termino -> factor termino'
            factor(factor);
            termino_prima(termino_prima);
            
            /* inicia accion 44 
            if(termino’.tipo == “float”) then{
                termino.h=termino’.h;
                termino.tipo=termino’.tipo; 
            }else{
                termino.h=factor.h;
                termino.tipo=factor.tipo; }*/
            
            if(analizarSemantica){
                if(termino_prima.tipo.equals("float")){
                    termino.h=termino_prima.h;
                    termino.tipo=termino_prima.tipo;
                }else{
                    termino.h=factor.h;
                    termino.tipo=factor.tipo;
                }
                
            }
            
            /* fin accion 44 */

        } else {

            error("[termino] Falto la definicion del factor al inicio del termino. Linea: "
                    + cmp.be.preAnalisis.numLinea);

        }

    }

//------------------------------------------------------------------------------
//Implementado por: Luis Alejandro Vazquez Saucedo  
//PROCEDURE término’
//PRIMERO(termino’) = {opmult}  
    private void termino_prima(Atributos termino_prima) {
        
        Atributos factor = new Atributos();
        Atributos termino_prima_1 = new Atributos();
        
        if (preAnalisis.equals("opmult")) {
            //termino’→ opmult
            emparejar("opmult");
            factor(factor);
            termino_prima(termino_prima_1); //recursividad
            
            /*accion semantica 45
            if(termino’1.tipo == “float”) then{
                termino’.h=termino’1.h;
                termino’.tipo=termino’1.tipo; 
            }else{
                termino’.h=factor.h;
                termino’.tipo=factor.tipo; }*/
            if(analizarSemantica){
                if(termino_prima_1.tipo.equals("float")){
                    termino_prima.h=termino_prima_1.h;
                    termino_prima.tipo=termino_prima_1.tipo;
                }else{
                    termino_prima.h=factor.h;
                    termino_prima.tipo=factor.tipo;
                }
            }
            /*fin accion semantica 45*/
            
            
        } else {
            /*accion semantica 46*/
            if(analizarSemantica){
                termino_prima.tipo = VACIO;
            }
            /*fin accion semantica 45*/
        }
    }
//—-----------------------------------------------------

//———————————————————————————————————————————————————————————————
//factor→ id  factor’ | num | num.num  |  ( expresion ) 
// Primeros factor = { id } ∪ { num } ∪ { num.num } ∪ { ( }
    private void factor(Atributos factor) {
        
        Atributos factor_prima = new Atributos();
        Atributos expresion = new Atributos();
        Linea_BE id = new Linea_BE();
        Linea_BE num = new Linea_BE();
        Linea_BE numnum= new Linea_BE();
        
        if (preAnalisis.equals("id")) {
            //factor→ id  factor’ 
                        
            id = cmp.be.preAnalisis;
            emparejar("id");
            
            factor_prima(factor_prima);
            
            /* accion semantica 47 
            Factor.tipo :=
            if buscaTipo (id.entrada) != nil AND factor’.tipo == VACIO then VACIO
            else ERROR_TIPO*/
            if(analizarSemantica){
                if((!cmp.ts.buscaTipo(id.entrada).equals("")) && factor_prima.tipo.equals(VACIO)){
                    factor.tipo = VACIO;
                }else{
                    factor.tipo=ERROR_TIPO;
            
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[factor] error en buscaTipo"
                                +cmp.ts.buscaTipo(id.entrada)+" o factor_prima"+factor_prima.tipo+". Linea:  " + cmp.be.preAnalisis.numLinea);
                    
                }
            }
            /* fin accion semantica 47 */
            
        } else if (preAnalisis.equals("num")) {
            // factor -> num
            num=cmp.be.preAnalisis;
            emparejar("num");
                    
            
            /* accion semantica 48 
            factor.h := num.lexema If esInt(num.lexema) then
                Factor.tipo := “int” else
                Factor.tipo := ERROR_TIPO*/
            if(analizarSemantica){
                factor.h = num.lexema;
//                System.out.println("lexema de num:"+num.lexema);
//                cmp.ts.buscaLexema(num.entrada).equals(factor.h)
                if(cmp.ts.buscaLexema(num.entrada).equals(factor.h)){
                    factor.tipo = VACIO;
                    factor.h="int";
                }else{
                    factor.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[factor] error en Tipo no es int"
                                +cmp.ts.buscaTipo(id.entrada)+". Linea:  " + cmp.be.preAnalisis.numLinea);
                    
                }
            }
            /* fin accion semantica 48 */
            
            
        } else if (preAnalisis.equals("num.num")) {
            //factor-> num.num
            numnum=cmp.be.preAnalisis;
            emparejar("num.num");
            
                        
            /* accion semantica 49 */
            if(analizarSemantica){
                factor.h = numnum.lexema;
                if(cmp.ts.buscaLexema(numnum.entrada).equals(factor.h)){
                    factor.tipo=VACIO;
                    factor.h = "float";
                }else{
                    factor.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[factor] error en tipo no es float"
                                +cmp.ts.buscaTipo(id.entrada)+". Linea:  " + cmp.be.preAnalisis.numLinea);
                   
                }
            }
            /* fin accion semantica 49 */ 
            
        } else if (preAnalisis.equals("(")) {
            emparejar("(");
            expresion(expresion);
            emparejar(")");
                        
            
            /* accion semantica 50 */
            if(analizarSemantica){
                factor.tipo = expresion.tipo;
            }
            /* fin accion semantica 50 */
            
            
        } else {
            error("[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    //PROCEDURE FACTOR'
    // PRIMERO factor’  = { ( } U  { empty }
    //Implementado por: Leonardo Zavala (21130874)
    private void factor_prima(Atributos factor_prima) {
        
        Atributos lista_expresiones = new Atributos();
        
        if (preAnalisis.equals("(")) {
            // factor' -> ( lista_expreisones )
            emparejar("(");
            lista_expresiones(lista_expresiones);
            emparejar(")");
            
            /* accion semantica 51 */
            if(analizarSemantica){
                factor_prima.tipo = lista_expresiones.tipo;
                
            }
            /* fin accion semantica 51 */
        } else {
            /* accion semantica 52 */
            if(analizarSemantica){
                factor_prima.tipo = VACIO;
            }
            /* fin accion semantica 52 */
            // factor' -> empty
        }
    }
    ////-----------------------------------------------------------------------
   
}
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
//::



