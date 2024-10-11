package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.*;

import java.util.*;

import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;   // Import the FileWriter class

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods

public class PublicElementsPrinter extends VoidVisitorWithDefaults<Void> {

    public FileWriter myWriter;
    public LinkedList<Integer> complexityList;

    PublicElementsPrinter(FileWriter  filename){

        myWriter = filename;
        complexityList = new LinkedList<Integer>();
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        Optional<PackageDeclaration> lepack = unit.getPackageDeclaration();
        
        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }

        
    }
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {

        for(MethodDeclaration method : declaration.getMethods()) {
            int complexity = calculateCyclomaticComplexity(method,arg);
            complexityList.addLast(complexity);
            String report = "classe= "+ declaration.getFullyQualifiedName().orElse("[Anonymous]") + " methode= " + method.getName().asString() + " complexite= " + Integer.toString(complexity) +"\n";
            
            try{
            myWriter.write(report); // Write data to file
            } catch (IOException e) {
                System.out.println("Error writing to the file: " + e.getMessage());
            }
        }
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {

        for(MethodDeclaration method : declaration.getMethods()) {
            int complexity = calculateCyclomaticComplexity(method,arg);
            complexityList.addLast(complexity);
            String report = "enum= "+ declaration.getFullyQualifiedName().orElse("[Anonymous]") + " methode= " + method.getName().asString() + " complexite= " + Integer.toString(complexity) +"\n";
            
            try{
            myWriter.write(report); // Write data to file
            } catch (IOException e) {
                System.out.println("Error writing to the file: " + e.getMessage());
            }
        }
    

    }
// non recursive cyclomatic complexity calcul
    public int calculateCyclomaticComplexity(MethodDeclaration method, Void arg){

        Optional<BlockStmt> block = method.getBody();

        if( block.isPresent()){
            return (1 + cyclomaticComplexity(block.get(), arg));
        }
        return 1;
    
    }
// scanning of the methode is recursive in case of a block found.
    public int cyclomaticComplexity(BlockStmt block, Void arg){
        int complexity = 0;


        Iterator<Statement> it =block.getStatements().iterator();
        while(it.hasNext()){
            Statement  statement = it.next();

            if( statement.isIfStmt()){
                complexity += 1; 
            }

            if( statement.isSwitchStmt() ){
                complexity += statement.asSwitchStmt().getEntries().size()-1; 
            }
            if( statement.isBlockStmt() ){
                complexity += this.cyclomaticComplexity(statement.asBlockStmt(), arg); 
            }
        } 

        return complexity;
        

    }
    public static void generatehist(LinkedList<Integer> list){
        int[] listhist= new int[]{0,0,0,0,0};
        String[] listLegend= new String[]{"1","2","3","4","5+"};

        for(Integer complex :list){
            if(complex==1){
                listhist[0]+=1;
            }
            if(complex==2){
                listhist[1]+=1;
            }
            if(complex==3){
                listhist[2]+=1;
            }
            if(complex==4){
                listhist[3]+=1;
            }
            if(complex==5){
                listhist[4]+=1;
            }

        }
        for(int i=0;i<5;i++){
            System.out.print(listLegend[i]+" : ");
            for(int j=0;j<30*listhist[i]/list.size();j++){
                System.out.print("*");
            }
            System.out.println("");
        }
        System.out.println(list.size());


    }
}

