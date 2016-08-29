var BooleanQuery = Java.type("org.apache.lucene.search.BooleanQuery");
var BooleanClause = Java.type("org.apache.lucene.search.BooleanClause");

function Parser(qparser) {
    function subq(k, v) {
        return qparser.subQuery(k + ":" + v, null).parse();
    }

    this.parse = function() {
        var querystring = qparser.getQstr();
        var terms = querystring.split(" ");
        var builder = new BooleanQuery.Builder();

        terms.forEach(function (termstr) {
            builder.add(subq("text", termstr), BooleanClause.Occur.MUST);
        });

        builder.add(subq("bool", true), BooleanClause.Occur.SHOULD);

        return builder.build();
    }
}
