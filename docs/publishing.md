# Public and instructor publishing

## Repositories

mars-book is the public source for Markdown, human interludes, package metadata, starter
projects, public checks, tooling, and approved figures. mars-book-instructor is a separate
private Git repository for full reference programs, alternative implementations, quiz
keys, private cases, and teaching notes. It is not a nested directory or submodule of
the public repository. Their histories and remotes remain separate.

The book's step-by-step instruction necessarily reveals how to build its examples.
Separating the complete reference and private checks does not make the demonstrated
algorithm secret. Public screenshots may show completed code as an intentional teaching
artifact. Private exam questions and cases should be designed with that distinction.

## Local and remote artifacts

The public build allowlists paths from package.json into a deterministic student ZIP.
Public Markdown assembly needs no private clone. The instructor verifier builds that
same public ZIP, records its digest, and produces a separate instructor ZIP with private
files and verification metadata. Keep instructor build outputs private and ignored by
Git. Never attach an instructor ZIP to a public release or workflow artifact.

The first publication is a source draft, not a final course release: captures, reader
review, final licensing, and MacTeX composition remain pending. Choose a book-content
license and code-example license explicitly before soliciting downstream reuse.

## Release sequence

1. Review the exact public diff, including images and metadata, for intended publication.
2. Validate and test tooling. Grade the reference and alternative against public and
   private contracts. Confirm a deliberate faulty implementation fails.
3. Record the public package digest and exact MARS CLI build used.
4. Generate and review real light-mode figures before calling a package print-ready.
5. Commit and push public source to the public remote; push instructor source only to
   its private remote. Verify GitHub visibility when creating or changing a remote.
6. Publish a versioned student archive. Share the instructor archive only through a
   professor-only channel chosen by the author.

CI in the public repo can validate and build without instructor secrets. A private CI
can clone the pinned public revision and run the instructor checks. The current local
verifier requires an explicitly supplied CLI JAR and JDK 25; a distributable, pinned MARS
CLI release location should be selected before automating that private CI download.
