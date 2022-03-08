# Orchestrus

## Todo list

### Parse

- [x] XMI standards
- [ ] Project config
  - .properties
  - .MF
  - plugin.x
  - ...
- [ ] Java files

### Run

- [ ] Extract References (hrefs)
  - [x] List folders (`Config.sourceFiles`)
  - [x] Separate source/local/external (protocol?!)
  - [x] Resolve source/local that can be solved
  - [ ] Sort out unresolvable -> UX for typing alternative ? (Storage ?)
- [ ] Extract Trace
  - [x] Extract Source/Local/External File artefact
    - [ ] Hardcoded typing "translation" in Config ?
  - [x] Build (multiended) links between Source/Local/External artefact
    - [ ] Source and Local: solve and use Xpath to  recover specific elements path
    - [ ] What about externals ? UX alternative ?
  - [ ] Solve specific elements path
  - [ ] Directly where possible: source, resolvable File artefact.
    - [ ] With UX for external ?
- [x] Store Trace in JSon
  - [x] Trace init links (IDs)
  - [x] Artefacts
  - [x] Links
  - [x] Typing: artefacts & links -> EngineeringDomain !! 
    - ("Translations" to  ApplicationDomain ?!)
  - [ ] Fragmentation: cluster Paths to shows dependency nests, like common ancestor in the tree (X)path.
  - [ ] WOT ELSE broo ?!!?

- [ ] Load trace
  - [ ] Types for artefacts and links (basically "names")
  - [ ] Artefacts & Fragments
  - [ ] Links

### Setup

- Config file
  - project = `com.cea.papyrus.glossary`
  - projectRoot = `R:\Coding\Git\orchestrus\data\GlossaryML-ReferenceML`
  - projectName = `GlossaryML`
  - projectDependencies = `com.cea.papyrus.referencemanagement`
- File extensions ?
  - Resolved conflictual FileArtefact paths
  - Link type "translations" btw EngTypes and AppTypes