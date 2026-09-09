**7. Initial Risk Register**

This register records project-specific risks identified at M1. Each risk
has a unique ID, a specific description, a probable cause, a probability
and impact rating, a priority level, a mitigation action, a contingency
action, an owner, and a status. The register is a live artefact which
will be reviewed and updated at every milestone.

*Highest-priority risk for M1 defence: RISK-001 (Scope Creep: Critical).
Reason: CivicConnect is replacing an informal process. Stakeholders are
likely to request additional features once they see the system taking
shape. Without a controlled baseline and a formal change process, scope
growth will silently consume the team\'s capacity and delay delivery.*

Priority scale: Critical = must act now, High = act before next
milestone, Medium = monitor and plan \| Low = monitor

  -------------------------------------------------------------------------------------------------------------------
  Risk ID    Description      Cause            Prob.    Impact   Priority       Mitigation        Contingency      
  ---------- ---------------- ---------------- -------- -------- -------------- ----------------- ---------------- --
  RISK-001   Scope creep,     No formal change High     High     **Critical**   Maintain a        Formally decline 
             stakeholders     process;                                          controlled scope  or defer the     
             request new      stakeholders may                                  baseline from M1. request. Record  
             features after   assume the team                                   All new requests  the decision in  
             scope is         can add features                                  go through a      the Engineering  
             baselined        without impact                                    formal change     Decision Log.    
                                                                                request and       Escalate to      
                                                                                impact analysis   lecturer if      
                                                                                before            unresolved.      
                                                                                acceptance.                        

  RISK-002   Team member      Illness,         Medium   High     **High**       All members       Redistribute     
             becomes          academic                                          understand the    work using the   
             unavailable for  pressure, or                                      full project, not team working     
             a significant    personal                                          only their own    agreement. Log   
             period           circumstances                                     tasks. Shared     the variance.    
                              reducing a                                        code reviews and  Notify the       
                              member\'s                                         documentation     lecturer if a    
                              contribution                                      reduce            milestone        
                                                                                single-member     deadline is at   
                                                                                dependency.       risk.            

  RISK-003   Role-Based       Access rules not Medium   High     **High**       Define access     Restrict         
             Access Control   clearly defined                                   rules per role    affected         
             (RBAC)           in requirements,                                  (Requester,       features to      
             implemented      or not tested                                     Staff,            read-only or     
             incorrectly      per role                                          Management) in M1 disable them.    
                                                                                requirements.     Log as a defect  
                                                                                Test each role in and apply a fix  
                                                                                isolation during  before any       
                                                                                M3.               further testing  
                                                                                                  or release.      

  RISK-004   Sensitive        Security treated Low      High     **High**       Security          Disable affected 
             request data     as a later                                        requirements      features         
             exposed through  concern;                                          defined in M1.    immediately.     
             weak             authentication                                    All data          Apply and verify 
             authentication   not enforced on                                   endpoints require fix. Record      
             or insecure      all data routes                                   authentication.   incident in the  
             endpoints                                                          No secrets        Risk Register as 
                                                                                committed to the  a materialised   
                                                                                repository.       risk.            

  RISK-005   Team selects an  Stack chosen for Medium   Medium   **Medium**     Stack decision    Reduce scope to  
             unfamiliar       interest rather                                   deferred to M2    core features.   
             technology       than team                                         with team         Record the       
             stack, causing   readiness;                                        capability        schedule impact  
             delays due to    learning time                                     assessment as a   as a variance.   
             the learning     underestimated                                    required input.   Document the     
             curve                                                              Prefer tools the  trade-off in the 
                                                                                team has already  Decision Log.    
                                                                                used.                              

  RISK-006   Free-tier        Platform limits  Medium   Medium   **Medium**     Research and      Switch to a      
             hosting or       not checked                                       document          lower-cost       
             database limits  before                                            free-tier limits  alternative.     
             reached, causing selection; usage                                  for all candidate Reduce data      
             unexpected       grows beyond the                                  platforms in M2   retention or     
             outage or cost   free tier during                                  decision records. feature scope as 
                              testing                                           Build within      needed. Document 
                                                                                those limits.     the change.      

  RISK-007   Requester        Notification     Medium   Medium   **Medium**     Define a          Provide in-app   
             notification     system not                                        notification NFR  status           
             delivery fails;  tested                                            with a measurable visibility as a  
             users do not     end-to-end;                                       acceptance        fallback so      
             receive status   reliance on                                       criterion. Test   requesters can   
             updates on their third-party                                       delivery in       still check      
             requests         email/SMS                                         staging before    their request.   
                              service without                                   release.          Log failures for 
                              fallback                                                            review.          

  RISK-008   AI-generated     Time pressure;   Medium   Medium   **Medium**     Every AI          Remove the       
             artefacts        team trusts AI                                    contribution is   unverified       
             (requirements,   output without                                    logged in the AI  artefact.        
             test cases,      checking                                          Usage Register. A Replace with a   
             code) used       accuracy against                                  team member       human-authored   
             without adequate project context                                   reviews and       version. Update  
             human review                                                       verifies the      the AI Usage     
                                                                                output before it  Register to      
                                                                                enters any        record what was  
                                                                                controlled        rejected and     
                                                                                artefact.         why.             
  -------------------------------------------------------------------------------------------------------------------

