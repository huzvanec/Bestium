interface Tag {
    name: string;
}

async function fetchGitHubVersions(owner: string, repo: string) {
    let response: Response;
    try {
        response = await fetch(`https://api.github.com/repos/${owner}/${repo}/tags`)
    } catch (e) {
        response = {ok: false} as Response
    }
    const json: Tag[] = response.ok ? await response.json() : [{name: "v0.0.0"}]
    return json.map(tag => tag.name.substring(1));
}

const bestiumVersions = await fetchGitHubVersions("huzvanec", "Bestium")
export const LATEST_BESTIUM = bestiumVersions[0]

const userdevVersions = await fetchGitHubVersions("PaperMC", "paperweight")
export const LATEST_USERDEV = userdevVersions[0]