package ru.fa;

import org.apache.commons.compress.utils.Lists;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.FBRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFLanguages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JenaTest {

    private static final String URI = "http://knbase.fa.ru#";
    private static final String CHILDREN = URI + "children";
    private static final String PARENT = URI + "parent";
    private static final String TYPE = URI + "type";
    private static final String LEVEL = URI + "level";

    private Model example;

    @BeforeEach
    void before() {
        example = ModelFactory.createDefaultModel();
    }

    @Test //677
    void test() {
        var bank = example.createResource(URI + "bank");
        var another = example.createResource(URI + "anotherBank");
        var childBank = example.createResource(URI + "childBank");
        var sber = example.createResource(URI + "Sber");
        var centerSber = example.createResource(URI + "CenterSber");
        var uralSber = example.createResource(URI + "UralSber");
        var siberiaSber = example.createResource(URI + "SiberiaSber");

        var type = example.createProperty(TYPE);

        bank.addProperty(type, "BANK");
        another.addProperty(type, "BANK");
        childBank.addProperty(type, "BANK");
        sber.addProperty(type, "BANK");
        centerSber.addProperty(type, "BANK");
        uralSber.addProperty(type, "BANK");
        siberiaSber.addProperty(type, "BANK");

        addChild(bank, another);
        addChild(bank, sber);
        addChild(bank, childBank);

        addChild(sber, centerSber);
        addChild(sber, uralSber);
        addChild(sber, siberiaSber);

        var card = example.createResource(URI + "card");
        var debit = example.createResource(URI + "debit");
        var credit = example.createResource(URI + "credit");
        var gold = example.createResource(URI + "gold");
        var plain = example.createResource(URI + "plain");

        card.addProperty(type, "CARD");
        debit.addProperty(type, "CARD");
        credit.addProperty(type, "CARD");
        gold.addProperty(type, "CARD");
        plain.addProperty(type, "CARD");

        addChild(card, debit);
        addChild(card, credit);

        addChild(debit, gold);
        addChild(debit, plain);

        var rule1 = example.createResource(URI + "rule1");
        var rule2 = example.createResource(URI + "rule2");
        var rule3 = example.createResource(URI + "rule3");
        var rule4 = example.createResource(URI + "rule4");
        var dimValueProperty = example.createProperty(URI + "dimValue");
        rule1.addProperty(dimValueProperty, bank);
        rule2.addProperty(dimValueProperty, sber);
        rule3.addProperty(dimValueProperty, siberiaSber);
        rule4.addProperty(dimValueProperty, bank);

        var dimCardProperty = example.createProperty(URI + "cardValue");
        rule1.addProperty(dimCardProperty, credit);
        rule2.addProperty(dimCardProperty, debit);
        rule3.addProperty(dimCardProperty, plain);
        rule4.addProperty(dimCardProperty, card);

        example.write(System.out, RDFLanguages.TTL.getName(), URI);

        String rules = "[r1: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#dimValue ?bc) " +
                "            (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#cardValue ?cc) " +
                "-> (?o http://knbase.fa.ru#dimValue ?bc)" +
                "   (?o http://knbase.fa.ru#cardValue ?cc)" +
                "]" +
                "[r2: (?o http://knbase.fa.ru#dimValue  ?b) noValue(?b http://knbase.fa.ru#children ?bc) " +
                "     (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#cardValue ?cc) " +
                "-> (?o http://knbase.fa.ru#cardValue ?cc)]" +
                "[r3: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#dimValue ?bc) " +
                "     (?o http://knbase.fa.ru#cardValue ?c) noValue(?c http://knbase.fa.ru#children ?cc) " +
                "-> (?o http://knbase.fa.ru#dimValue ?bc)]";

//        String rules = "[r1: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) " +
//                "            (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) " +
//                "-> (?o http://knbase.fa.ru#dimValue ?bc)" +
//                "   (?o http://knbase.fa.ru#cardValue ?cc)" +
//                "]";

//        String rules = "[r1: (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#dimValue ?bc)" +
//                "            (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) -> (?o http://knbase.fa.ru#dimValue ?bc) (?o http://knbase.fa.ru#cardValue ?cc)] " +
////                "       [r2: (?o http://knbase.fa.ru#cardValue ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#cardValue ?cc)" +
////                "            (?o http://knbase.fa.ru#dimValue  ?b) (?b http://knbase.fa.ru#children ?bc) -> (?o http://knbase.fa.ru#cardValue ?cc) (?o http://knbase.fa.ru#dimValue ?bc)]" +
//                "";

//        String rules = "[r1: (?b http://knbase.fa.ru#type 'BANK') (?c http://knbase.fa.ru#type 'CARD') " +
//                "(?b http://knbase.fa.ru#parent ?bp) (?c http://knbase.fa.ru#parent ?cp) " +
//                "(?o http://knbase.fa.ru#dimValue ?b) noValue(?o http://knbase.fa.ru#cardValue ?c) " +
//                "(?o1 http://knbase.fa.ru#dimValue ?bp) (?o1 http://knbase.fa.ru#cardValue ?cp) -> (?o1 http://knbase.fa.ru#dimValue ?b) (?o1 http://knbase.fa.ru#cardValue ?c)]" +
//                "" +
//                "[r2: (?b http://knbase.fa.ru#type 'BANK') (?c http://knbase.fa.ru#type 'CARD') " +
//                "(?b http://knbase.fa.ru#parent ?bp) (?c http://knbase.fa.ru#parent ?cp) " +
//                "noValue(?o http://knbase.fa.ru#dimValue ?b) (?o http://knbase.fa.ru#cardValue ?c) " +
//                "(?o1 http://knbase.fa.ru#dimValue ?bp) (?o1 http://knbase.fa.ru#cardValue ?cp) -> (?o1 http://knbase.fa.ru#dimValue ?b) (?o1 http://knbase.fa.ru#cardValue ?c)]" +
//                "" +
//                "[r3: (?b http://knbase.fa.ru#type 'BANK') (?c http://knbase.fa.ru#type 'CARD') " +
//                "(?b http://knbase.fa.ru#parent ?bp) (?c http://knbase.fa.ru#parent ?cp) " +
//                "noValue(?o http://knbase.fa.ru#dimValue ?b) noValue(?o http://knbase.fa.ru#cardValue ?c) " +
//                "(?o1 http://knbase.fa.ru#dimValue ?bp) (?o1 http://knbase.fa.ru#cardValue ?cp) -> (?o1 http://knbase.fa.ru#dimValue ?b) (?o1 http://knbase.fa.ru#cardValue ?c)]";
        List<Rule> ruleList = Rule.parseRules(rules);
        Reasoner genericRulesReasoner = new GenericRuleReasoner(ruleList);
        InfModel rulesModel = ModelFactory.createInfModel(genericRulesReasoner, example);

        System.out.println("Statements");
        var rulesList = rulesModel.listStatements();
        while (rulesList.hasNext()) {
            System.out.println(rulesList.next());
        }

        System.out.println("Get property rule1");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule1.getURI()).listProperties()));

        System.out.println("Get property rule2");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule2.getURI()).listProperties()));

        System.out.println("Get property rule3");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule3.getURI()).listProperties()));

        System.out.println("Get property rule4");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule4.getURI()).listProperties()));
    }

    // пример из фото телеграма
    @Test
    void test2() {
        var childrenProperty = example.createProperty(CHILDREN);

        var node5 = createNode("5");
        var node11 = createNode("11");
        var node12 = createNode("12");
        var node3 = createNode("3");
        var node4 = createNode("4");
        var node1 = createNode("1");
        var node2 = createNode("2");

        addChild( node5, node12);
        addChild( node5, node11);
        addChild( node1, node3);
        addChild( node2, node4);
        addChild( node1, node2);
        addChild( node2, node5);

        var node8 = createNode("8");
        var node9 = createNode("9");
        var node6 = createNode("6");
        var node10 = createNode("10");
        var node7 = createNode("7");

        addChild( node7, node10);
        addChild( node6, node7);
        addChild( node7, node9);
        addChild( node6, node8);

        var rule3 = example.createResource(URI + "rule3");
        var rule2 = example.createResource(URI + "rule2");
        var rule1 = example.createResource(URI + "rule1");

        var firstValueProperty = example.createProperty(URI + "firstValue");
        var secondValueProperty = example.createProperty(URI + "secondValue");

        rule1.addProperty(firstValueProperty, node2);
        rule1.addProperty(secondValueProperty, node10);

        rule2.addProperty(firstValueProperty, node4);
        rule2.addProperty(secondValueProperty, node9);

        rule3.addProperty(firstValueProperty, node1);
        rule3.addProperty(secondValueProperty, node6);

        String rules = "" +
                "[r1: (?o http://knbase.fa.ru#firstValue  ?b)  (?b http://knbase.fa.ru#children ?bc) noValue(?o1 http://knbase.fa.ru#firstValue ?bc) " +
                "        ->  (?o http://knbase.fa.ru#firstValue ?bc) ]" +
                "[r2: (?o http://knbase.fa.ru#secondValue  ?c) (?c http://knbase.fa.ru#children ?cc) noValue(?o1 http://knbase.fa.ru#secondValue ?cc)" +
                "     -> (?o http://knbase.fa.ru#secondValue ?cc) ]";

        List<Rule> ruleList = Rule.parseRules(rules);
        FBRuleReasoner genericRulesReasoner = new GenericRuleReasoner(ruleList);
        InfModel rulesModel = ModelFactory.createInfModel(genericRulesReasoner, example);

        System.out.println("Statements");
        var rulesList = rulesModel.listStatements();
        while (rulesList.hasNext()) {
            System.out.println(rulesList.next());
        }

        System.out.println("Get property rule1");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule1.getURI()).listProperties()));

        System.out.println("Get property rule2");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule2.getURI()).listProperties()));

        System.out.println("Get property rule3");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule3.getURI()).listProperties()));
    }

    @Test
    void testReflection() {
        var rootruleProperty = example.createProperty(URI + "firstRoot");
        var firstValueProperty = example.createProperty(URI + "firstValue");

        var nilrule = example.createResource(URI + "nilrule");

        var nilNode = createNode("nil", -1, nilrule);
        nilrule.addProperty(rootruleProperty, nilNode);

        var node1 = createNode("1", 0, nilrule);
        var node2 = createNode("2", 2, nilrule);
        var node3 = createNode("3", 2, nilrule);
        var node4 = createNode("4", 3, nilrule);
        var node5 = createNode("5", 3, nilrule);
        var node11 = createNode("11", 4, nilrule);
        var node12 = createNode("12", 4, nilrule);

        addChild( node5, node12);
        addChild( node5, node11);
        addChild( node1, node3);
        addChild( node2, node4);
        addChild( node2, node5);
        addChild( node1, node2);

        var rule1 = example.createResource(URI + "rule1");
        rule1.addProperty(firstValueProperty, node1);
        rule1.addProperty(rootruleProperty, node1);

        var rule2 = example.createResource(URI + "rule2");
        rule2.addProperty(firstValueProperty, node2);
        rule2.addProperty(rootruleProperty, node2);

        String rules = "[r1: (?o http://knbase.fa.ru#firstValue ?d) (?d http://knbase.fa.ru#children ?c)" +
                "            (?o http://knbase.fa.ru#firstRoot ?or) (?or http://knbase.fa.ru#level ?orl)" +
                "           (?o1 http://knbase.fa.ru#firstValue ?c) (?o1 http://knbase.fa.ru#firstRoot ?o1r) (?o1r http://knbase.fa.ru#level ?o1rl)" +
                "           greaterThan(?orl, ?o1rl) " +
                "-> " +
//                "print(?o, ?d, ?c, ?or, ?orl, ?o1, ?c, ?o1r, ?o1rl) " +
                "(?o http://knbase.fa.ru#firstValue ?c) remove(4)" +
                "]";

//        String rules = "[r1: (?o http://knbase.fa.ru#firstValue ?d) (?d http://knbase.fa.ru#children ?c)" +
//                "            (?o http://knbase.fa.ru#firstRoot ?or) (?or http://knbase.fa.ru#level ?orl)" +
//                "        -> remove(0)]";

        List<Rule> ruleList = Rule.parseRules(rules);
        FBRuleReasoner genericRulesReasoner = new GenericRuleReasoner(ruleList);
//        genericRulesReasoner.setTraceOn(true);
//        genericRulesReasoner.setDerivationLogging(true);
        InfModel rulesModel = ModelFactory.createInfModel(genericRulesReasoner, example);

        System.out.println("Statements");
        var rulesList = rulesModel.listStatements();
        System.out.println("Statements DONE");
        while (rulesList.hasNext()) {
            System.out.println(rulesList.next());
        }

        System.out.println("Get property rule1");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule1.getURI()).listProperties()));

        System.out.println("Get property rule2");
        System.out.println(Lists.newArrayList(rulesModel.getResource(rule2.getURI()).listProperties()));
    }

    private void addChild(Resource parent, Resource child) {
        var childrenProperty = example.createProperty(CHILDREN);
        var parentProperty = example.createProperty(PARENT);
        parent.addProperty(childrenProperty, child);
        child.addProperty(parentProperty, parent);
    }

    private Resource createNode(String index, int level, Resource nilrule) {
        var node = example.createResource(URI + index);
        var childrenProperty = example.createProperty(CHILDREN);
        var levelProperty = example.createProperty(LEVEL);
        var firstValueProperty = example.createProperty(URI + "firstValue");
        node.addProperty(levelProperty, Integer.toString(level), XSDDatatype.XSDinteger);
//        node.addProperty(childrenProperty, node);
        nilrule.addProperty(firstValueProperty, node);
        return node;
    }

    private Resource createNode(String index) {
        var node = example.createResource(URI + index);
//        var childrenProperty = example.createProperty(CHILDREN);
//        node.addProperty(childrenProperty, node);
        return node;
    }
}
