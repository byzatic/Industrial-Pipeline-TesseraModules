grammar MyDsl;

script: command+ EOF;

command
  : getCommand
  | processCommand
  | putCommand
  ;

getCommand
  : 'GET FROM' source=sourceType
    'STORAGE' storage=ID
    modifierClause?
    'BY DATA ID' dataId=ID
    aliasClause? ';'
  ;

sourceType
  : 'SELF'
  | 'CHILD' child=ID
  ;

processCommand
  : 'PROCESS FUNCTION' function=ID
    argsClause?
    'RETURN' resultId=ID ';'
  ;

putCommand
  : 'PUT DATA' localDataId=ID
    'TO STORAGE' storage=ID
    modifierClause?
    'BY DATA ID' dataId=ID ';'
  ;

modifierClause: 'MODIFIER' mod=('local' | 'global');
aliasClause: 'AS' alias=ID;
argsClause: '(' argList? ')';
argList: STRING (',' STRING)*;

// Лексические правила
ID: [a-zA-Z0-9] [a-zA-Z0-9_-]*;
STRING: '"' (~["\\] | '\\' .)* '"';
WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;