# kamp

kamp serves data and handles HTML generation for my personal site/portfolio, built with Ktor and [kapsule](https://github.com/sakethpathike/kapsule).

kamp also implements a snapshot _engine_ that pushes everything it would serve: HTML, assets, all of it, to [sakethpathike.github.io](https://github.com/sakethpathike/sakethpathike.github.io). This makes sure the site stays up even if the server isn’t. Snapshots get auto-pushed on every commit to the `master` branch.

The blog _posts_ are standard Markdown files, parsed by a custom parser I wrote (based on [this spec](https://spec.commonmark.org/0.31.2/#appendix-a-parsing-strategy), but not strictly following it). kapsule generates HTML from the parsed Markdown nodes.

During the build, Open Graph images are auto-generated using Java’s `BufferedImage` and `ImageIO`.

### kamp?

*kamp* as in _camp_. Maybe not Clemens Point or Horseshoe Overlook, but on the internet.
