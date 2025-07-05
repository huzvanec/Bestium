interface Commit {
    sha: string;
    url: string;
}

interface Tag {
    name: string;
    commit: Commit
}

const VERSION_PLACEHOLDER = "LATEST_VERSION"

async function fetchGitHubVersions(owner: string, repo: string): Promise<Tag[]> {
    let response: Response;
    try {
        response = await fetch(`https://api.github.com/repos/${owner}/${repo}/tags`)
    } catch (e) {
        response = {ok: false} as Response
    }
    return response.ok ? await response.json() : []
}

const bestiumTags = await fetchGitHubVersions("huzvanec", "Bestium")
export const LATEST_BESTIUM: string = bestiumTags[0]?.name.substring(1) ?? VERSION_PLACEHOLDER

const userdevTags = await fetchGitHubVersions("PaperMC", "paperweight")
export const LATEST_USERDEV: string = userdevTags[0]?.name.substring(1) ?? VERSION_PLACEHOLDER

const PAPER_VERSION_PLACEHOLDER = VERSION_PLACEHOLDER + "-R0.1-SNAPSHOT"

async function fetchPaperVersion(): Promise<string> {
    if (!bestiumTags[0]) return PAPER_VERSION_PLACEHOLDER
    const treeSha = bestiumTags[0].commit.sha
    let response: Response
    try {
        response = await fetch(`https://raw.githubusercontent.com/huzvanec/Bestium/${treeSha}/gradle/libs.versions.toml`)
    } catch (e) {
        response = {ok: false} as Response
    }
    const toml = await response.text()
    const match = toml.match(/^\s*paper\s*=\s*"([^"]+)"/m);
    const paperVersion = match ? match[1] : null;
    return paperVersion ?? PAPER_VERSION_PLACEHOLDER
}

export const BESTIUM_PAPER: string = await fetchPaperVersion()