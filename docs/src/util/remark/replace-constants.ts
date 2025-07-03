import type { RemarkPlugin } from "@astrojs/markdown-remark";
import { visit } from "unist-util-visit";

// ❤️ https://github.com/PaperMC/docs/blob/1954c2fb179a628035f49974bd96b06fcefaa51e/src/utils/remark/code_const.ts
export const plugin: RemarkPlugin = ({ constants }: { constants: { [key: string]: string } }) => {
    return (tree) => {
        visit(tree, ["code", "inlineCode"], (node) => {
            if (node.type === "code" && !node.meta?.includes("replace")) {
                return;
            }

            const code = node as { value: string };
            code.value = code.value.replace(/%\{([^}]+)\}%/g, (_: string, name: string) => {
                return constants[name] ?? name
            })
        });
    };
};