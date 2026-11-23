# Independent expression evaluator

Replace eval_expr in expression.asm. The grammar, numeric bounds, diagnostic order and public interface are specified in the written expression companion. Keep the complete caller and public labels. Recursive descent, precedence climbing and equivalent explicit-stack designs are accepted; private labels and scratch-register allocation are not graded.

The four native assessments contain 251 successful original inputs, 289 original failures, six new public recovery sequences and twenty caller variants. Every original direct-call input remains, including selected arena, offset, sentinels and return continuation. Long case display names retain the stable original index and a hexadecimal preview; their complete bytes remain in the fixture. Naming bounds do not truncate the input.

Reads belong wholly to the selected byte slice among caller objects. All caller-data stores are forbidden, including writes later restored. Private tables and a correctly restored frame are permitted. Restore full saved registers, fp, sp and the incoming return link. Repeated calls get new pointer/length arguments and temporary-register sentinels without resetting the machine.

Optional CLI checks, with JDK 25 from this directory:

    java -jar /path/to/cli.jar grade kernel.kts expression.asm
    java -jar /path/to/cli.jar grade caller.kts expression.asm

The six additional public recovery sequences run through the native assessment. The caller script uses the same public observation engine as the native checker and requires the MARS edition supporting console format 8. Bounded instruction budgets stop runaway execution; exact instruction counts are not graded. The instructor distribution keeps its additional cases and solutions separate.
