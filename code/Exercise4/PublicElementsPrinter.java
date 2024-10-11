package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import com.github.javaparser.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;   // Import the FileWriter class


// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods

public class PublicElementsPrinter extends VoidVisitorWithDefaults<Void> {
    public ArrayList<ArrayList<ArrayList<String>>> list ;
    public int curent_class ;
    public String current_package ;
    public FileWriter myWriter;

    PublicElementsPrinter(FileWriter  filename){
        curent_class=0;
        current_package="";
        list=new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<ArrayList<String>> classList = new ArrayList<ArrayList<String>>();


        myWriter = filename;


    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        Optional<PackageDeclaration> lepack = unit.getPackageDeclaration();
        if(lepack.isPresent()){
            current_package=lepack.get().getName().asString();
        }
        else{
                current_package="has no package";
        }
        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
        
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
        if(!declaration.isPublic()) return;

        ArrayList<ArrayList<String>> classList = new ArrayList<ArrayList<String>>();

        ArrayList<String> className = new ArrayList<String>();
        ArrayList<String> attributeName = new ArrayList<String>();
        ArrayList<String> methodName = new ArrayList<String>();

        className.add(declaration.getFullyQualifiedName().orElse("[Anonymous]"));
        classList.add(className);
        classList.add(attributeName);
        classList.add(methodName);

        list.add(classList);

        for(MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }
        // Printing nested types in the top level
        for(BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration)
                member.accept(this, arg);
        }
        for(FieldDeclaration member : declaration.getFields()) {
                member.accept(this, arg);
        }

        for(String attribute :  list.get(curent_class).get(2)){
            boolean has_public_Getter = false;
            for(String method :  list.get(curent_class).get(1)){
                if(attribute.equals(method.substring(3).toLowerCase())){
                    has_public_Getter=true;
                    
                }
            }

            String requete = "package= "+current_package +" class= "+ list.get(curent_class).get(0).get(0) + " attribute= " +attribute + " hasgetter= " + (has_public_Getter ? "true": "false") +"\n";
            System.out.println(requete);
            try {
            myWriter.write(requete); // Write data to file
            } catch (IOException e) {
                System.out.println("Error writing to the file: " + e.getMessage());
            }

            
        }

        curent_class++ ;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
        
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {

        if(!declaration.isPublic()) return;

        String dec = declaration.getName().asString();
    
        if(!dec.substring(0,3).equals("get")) return;

        list.get(curent_class).get(1).add(declaration.getName().asString());
    }

    @Override
    public void visit(FieldDeclaration declaration, Void arg) {

        
        if(declaration.isPublic()) return;
        declaration.getVariables().forEach((k)-> list.get(curent_class).get(2).add(k.getName().asString() ) );

    }
}
