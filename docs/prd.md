# Product Requirements Document (PRD): Tidepool

**Team:** `Group 5` · **Repo:** `https://github.com/cst-438-p3-g5/sea-animal-discovery` · **Last updated:** 2026-11-02

---

## 1. Users

- **Primary user:** A beginner tidepool or beach explorer who encounters unfamiliar sea animals and wants to identify them and keep a personal record of their discoveries.

## 2. Problem

Beginner tidepool explorers often struggle to identify unfamiliar sea animals and organize records of what they discover at the beach. Existing resources may provide animal information but do not offer a simple, unified way to explore species profiles and save personal sightings.

## 3. Success measures

| **#** | **Measure** | **Target** | **How we check it** |
|---|---|---|---|
| S1 | Find a species profile and record a sighting | Under 2 minutes | Timed usability test with 3 new users |
| S2 | Users can view and manage their saved sightings | The owner can create, view, edit, and delete a sighting; another user is denied access to it | Automated API and client tests for owner actions and cross-user access |
| S3 | Main discovery and sighting flow works on web and Android | 100% of required flow steps work on both platforms | Playwright web test and manual Android testing |

## 4. Scope: MVP, vertical slice, and not building

- **MVP stories:** PRD-1, PRD-2, PRD-3, PRD-4, PRD-5, PRD-6.
- **Vertical slice:** PRD-3, recording a sighting. A user browses the species catalog, opens a profile, signs in, records a sighting, and sees it in "My Sightings." This will use the real database, API, and web client, with Playwright and accessibility testing, and will also be supported on Android.
- **Not building (this term):** Social feeds, comments or likes; AI-powered animal identification; public sighting maps or GPS tracking; offline synchronization; push notifications; and iOS support.

## 5. User stories and acceptance criteria

| **ID** | **Story** | **Acceptance criteria** |
|---|---|---|
| PRD-1 | As a user, I want to sign in with GitHub or Google so that I can securely save my discoveries. | **Given** I have a verified provider account, **when** I sign in successfully, **then** I can access my personal account. **Given** my provider does not verify my email, **when** I try to sign in, **then** the API rejects the sign-in and does not create an account. **Given** my verified email is already registered through the other provider, **when** I try to sign in, **then** the API returns 409 and identifies the existing provider. |
| PRD-2 | As a user, I want to update my profile and delete my account so that I can manage my personal information. | **Given** I am signed in, **when** I update my profile, **then** my changes are saved. **When** I delete my account, **then** my profile and personal sightings are removed. |
| PRD-3 | As a user, I want to record a sea animal sighting so that I can remember what I discovered. | **Given** I am signed in and viewing a species, **when** I submit a valid sighting with a date, **then** it is saved and linked to my account and that species. |
| PRD-4 | As a visitor, I want to browse sea animal profiles so that I can identify animals and learn about their habitats. | **Given** the catalog contains species, **when** I select an animal, **then** I see its name, identification details, habitat, and conservation information. |
| PRD-5 | As a user, I want to view my saved sightings so that I can revisit my discoveries. | **Given** I have recorded sightings, **when** I open "My Sightings," **then** I see only my sightings with their species and dates. |
| PRD-6 | As a user, I want to edit or delete my sightings so that my records remain accurate. | **Given** I own a sighting, **when** I edit or delete it, **then** my changes are saved or the sighting is removed. **Given** a sighting belongs to another user, **when** I try to view or change it, **then** the API returns 403 or 404 and the sighting remains unchanged. |

---

### Change log

| **Date** | **PR** | **What changed and why** |
|---|---|---|
| 2026-11-02 | 1 | Initial PRD based on the team's project plan. |
| 2026-11-02 | 2 | Clarified the S2 test target, added OAuth email rejection criteria, and made cross-user sighting protection testable. |
